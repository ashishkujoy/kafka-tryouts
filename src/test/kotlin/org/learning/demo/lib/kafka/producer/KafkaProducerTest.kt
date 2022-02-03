package org.learning.demo.lib.kafka.producer

import io.kotest.matchers.nulls.shouldBeNull
import org.junit.jupiter.api.Test
import org.learning.demo.IntegrationTest
import org.learning.demo.lib.kafka.KafkaMessage
import org.springframework.beans.factory.annotation.Autowired
import reactor.test.StepVerifier

@IntegrationTest
class KafkaProducerTest(@Autowired private val kafkaProducer: KafkaProducer) {

    @Test
    fun `should produce message to kafka topic`() {
        val senderResultMono = kafkaProducer.produce(
            topic = "integration-test",
            message = KafkaMessage(
                eventId = "EV-001",
                eventName = "kafka-producer-integration-test",
                createdTime = "2020-01-01",
                partitionKey = "1",
                identifiers = mapOf(
                    "key1" to "v1"
                ),
                payload = mapOf(
                    "message" to "hello world"
                )
            )
        )

        StepVerifier.create(senderResultMono)
            .assertNext { senderResult ->
                senderResult.exception().shouldBeNull()
            }
            .verifyComplete()
    }
}