package org.learning.demo.lib.producer

import io.kotest.assertions.timing.EventuallyConfig
import io.kotest.assertions.timing.eventually
import io.kotest.assertions.until.FixedInterval
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9092", "port=9092"])
@ExperimentalTime
class KafkaConsumerTest(
    @Autowired private val kafkaProducer: KafkaProducer,
    @Autowired private val dummyKafkaConsumer: DummyKafkaConsumer
) {
    @Test
    fun `should consumer message`() {
        val message = KafkaMessage(
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

        kafkaProducer
            .produce(topic = "integration-test", message = message)
            .flatMap {
                kafkaProducer.produce(topic = "integration-test-v2", message.copy(eventId = "EB-002"))
            }
            .block()

        runBlocking {
            eventually(
                EventuallyConfig(
                    duration = 5.toDuration(DurationUnit.SECONDS),
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
}