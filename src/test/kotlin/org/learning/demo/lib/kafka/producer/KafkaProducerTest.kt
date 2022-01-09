package org.learning.demo.lib.kafka.producer

import io.kotest.matchers.nulls.shouldBeNull
import org.junit.jupiter.api.Test
import org.learning.demo.lib.kafka.KafkaMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import reactor.test.StepVerifier

@SpringBootTest
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
@EmbeddedKafka(partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9092", "port=9092"])
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