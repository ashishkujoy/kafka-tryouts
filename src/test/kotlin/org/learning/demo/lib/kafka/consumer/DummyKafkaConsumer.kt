package org.learning.demo.lib.kafka.consumer

import org.learning.demo.lib.kafka.KafkaConfig
import org.learning.demo.lib.kafka.KafkaMessage
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

@Component
class DummyKafkaConsumer(
    kafkaConfig: KafkaConfig,
) : KafkaConsumer(kafkaConfig, ConsumerConfig(
    groupId = UUID.randomUUID().toString(),
    consumerId = UUID.randomUUID().toString(),
    readPreference = ""
), listOf("integration-test", "integration-test-v2"), emptyMap()) {
    private val messages = mutableListOf<Triple<String, Map<String, String>, KafkaMessage>>()

    override fun processMessage(topicName: String, headers: Map<String, String>, message: KafkaMessage, isNotProcessedVerified: Boolean): Mono<Any?> {
        messages.add(Triple(topicName, headers, message))
        return Mono.just(true)
    }

    fun clearAllMessages() {
        messages.removeAll { true }
    }

    fun getAllMessages() = messages.toList()
}