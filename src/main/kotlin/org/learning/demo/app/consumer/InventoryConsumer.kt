package org.learning.demo.app.consumer

import com.fasterxml.jackson.module.kotlin.convertValue
import org.learning.demo.app.domain.event.OrderCreatedEvent
import org.learning.demo.app.service.InventoryService
import org.learning.demo.lib.kafka.KafkaConfig
import org.learning.demo.lib.kafka.KafkaMessage
import org.learning.demo.lib.kafka.consumer.ConsumerConfig
import org.learning.demo.lib.kafka.consumer.KafkaConsumer
import org.learning.demo.lib.kafka.serializer.ObjectMapperCache.objectMapper
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

@Component
class InventoryConsumer(kafkaConfig: KafkaConfig, private val inventoryService: InventoryService) : KafkaConsumer(
    kafkaConfig = kafkaConfig,
    consumerConfig = ConsumerConfig(
        groupId = "inventory-consumer-group",
        consumerId = UUID.randomUUID().toString(),
        readPreference = "latest"
    ),
    topicsToConsume = listOf("order"),
    additionalConsumerProps = emptyMap()
) {
    override fun processMessage(topicName: String, headers: Map<String, String>, message: KafkaMessage): Mono<Any?> {

        return Mono.fromCallable { objectMapper.convertValue<OrderCreatedEvent>(message.payload) }
            .flatMap(inventoryService::processOrderCreatedEvent)
            .map { true }
    }
}