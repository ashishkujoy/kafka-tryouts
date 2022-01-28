package org.learning.demo.util

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.learning.demo.lib.kafka.KafkaMessage
import org.learning.demo.lib.kafka.serializer.KafkaMessageDeserializer
import org.learning.demo.lib.kafka.serializer.PartitionKeyDeserializer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import java.time.Duration

@Component
class EmbeddedKafkaConsumer {
    private val brokerUrl = "plaintext://localhost:9092"
    private val log = LoggerFactory.getLogger(this::class.java)

    private val customProperties = mapOf<String, Any>(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to brokerUrl,
        ConsumerConfig.CLIENT_ID_CONFIG to "test-client",
        ConsumerConfig.GROUP_ID_CONFIG to "test-group",
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to PartitionKeyDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to KafkaMessageDeserializer::class.java
    )

    fun readNextMessage(topicName: String, eventName: String, numberOfMessageToRead: Int = 1): List<KafkaMessage> {
        val kafkaReceiver: KafkaReceiver<String, KafkaMessage> = kafkaReceiver(topicName)

        val messages = kafkaReceiver.receiveAtmostOnce()
            .filter { it.value().eventName == eventName }
            .map { it.value() }
            .take(numberOfMessageToRead.toLong())
            .collectList()
            .timeout(Duration.ofSeconds(5))
            .onErrorResume { e ->
                log.error("Error while reading message from topic $topicName", e)
                Mono.just(emptyList()) }
            .block() ?: emptyList()

        println("******* Received messages: $messages")

        return messages
    }

    fun clearTopic(topicName: String) {
        kafkaReceiver(topicName).receive()
            .flatMap {
                it.receiverOffset().commit().map { 1 }
            }
            .timeout(Duration.ofSeconds(1))
            .onErrorResume { Flux.just(1) }
            .blockLast()
    }

    private fun kafkaReceiver(topicName: String): KafkaReceiver<String, KafkaMessage> {
        val receiverOptions = ReceiverOptions.create<String, KafkaMessage>(customProperties)
            .subscription(listOf(topicName))
        return KafkaReceiver.create(receiverOptions)
    }
}