package org.learning.demo.lib.kafka.consumer

import io.kotest.assertions.timing.EventuallyConfig
import io.kotest.assertions.timing.eventually
import io.kotest.assertions.until.FixedInterval
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.learning.demo.IntegrationTest
import org.learning.demo.lib.kafka.KafkaMessage
import org.learning.demo.lib.kafka.producer.KafkaProducer
import org.learning.demo.lib.kafka.repository.ProcessedMessageAudit
import org.learning.demo.lib.kafka.repository.ProcessedMessageAuditRepository
import org.learning.demo.util.assertNextWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDateTime
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@IntegrationTest
class KafkaConsumerTest(
    @Autowired private val kafkaProducer: KafkaProducer,
    @Autowired private val dummyKafkaConsumer: DummyKafkaConsumer,
    @Autowired private val processedMessageAuditRepository: ProcessedMessageAuditRepository,
) {

    @BeforeEach
    fun setUp() {
        processedMessageAuditRepository.deleteAll().block()
    }

    private val message = KafkaMessage(
        eventId = "EB-001",
        eventName = "consumer-event",
        createdTime = "2020-01-01",
        partitionKey = "12",
        identifiers = mapOf(
            "key1" to "val1"
        ),
        payload = mapOf(
            "message" to "hello"
        )
    )

    @Test
    fun `should consume new message only`() {
        processedMessageAuditRepository.save(
            ProcessedMessageAudit(
                eventId = "EB-000",
                processedAt = LocalDateTime.now(),
                consumerId = "foo"
            )
        ).block()

        kafkaProducer
            .produce(topic = "integration-test", message = message)
            .flatMap {
                kafkaProducer.produce(topic = "integration-test-v2", message.copy(eventId = "EB-000"))
            }
            .flatMap {
                kafkaProducer.produce(topic = "integration-test-v2", message.copy(eventId = "EB-002"))
            }
            .block()

        runBlocking {
            eventually(
                EventuallyConfig(
                    duration = 10.toDuration(DurationUnit.SECONDS),
                    interval = FixedInterval(1.toDuration(DurationUnit.SECONDS))
                )
            ) {
                val messages = dummyKafkaConsumer.getAllMessages()

                messages shouldHaveSize 2
                messages shouldContainExactlyInAnyOrder listOf(
                    Triple("integration-test", emptyMap(), message),
                    Triple("integration-test-v2", emptyMap(), message.copy(eventId = "EB-002")),
                )
            }
        }
    }

    @Test
    fun `should save audit of processed message`() {
        processedMessageAuditRepository.save(
            ProcessedMessageAudit(
                eventId = "EB-000",
                processedAt = LocalDateTime.now(),
                consumerId = "foo"
            )
        ).block()

        kafkaProducer
            .produce(topic = "integration-test", message = message)
            .flatMap {
                kafkaProducer.produce(topic = "integration-test-v2", message.copy(eventId = "EB-002"))
            }
            .block()

        runBlocking {
            eventually(
                EventuallyConfig(
                    duration = 10.toDuration(DurationUnit.SECONDS),
                    interval = FixedInterval(1.toDuration(DurationUnit.SECONDS))
                )
            ) {
                assertNextWith(processedMessageAuditRepository.findAll().collectList()) {
                    it.map { r -> r.eventId } shouldContainExactlyInAnyOrder listOf("EB-000", "EB-001", "EB-002")
                }
            }
        }
    }
}