package org.learning.demo.app.repository

import com.fasterxml.jackson.module.kotlin.convertValue
import org.learning.demo.app.domain.Order
import org.learning.demo.app.domain.event.OrderCreatedEvent
import org.learning.demo.lib.kafka.KafkaMessage
import org.learning.demo.lib.kafka.producer.KafkaProducer
import org.learning.demo.lib.kafka.serializer.ObjectMapperCache
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

@Component
class OrderPersistorAndEventPublisher(
    private val orderRepository: OrderRepository,
    private val kafkaProducer: KafkaProducer,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun saveAndPublishEvents(order: Order): Mono<Order> {
        return orderRepository.save(order)
            .doOnSuccess { log.info("Successfully save order in db") }
            .doOnError { log.error("Failed to save order in db", it) }
            .flatMap { savedOrder -> publishMessages(order).then(Mono.just(savedOrder)) }
    }

    private fun publishMessages(order: Order): Mono<Order> {
        return Flux.fromIterable(order.events.toList())
            .flatMap { event ->
                event as OrderCreatedEvent
                val message = KafkaMessage(
                    eventId = UUID.randomUUID().toString(),
                    eventName = event.eventName,
                    createdTime = LocalDateTime.now().toString(),
                    partitionKey = event.orderId,
                    identifiers = mapOf(),
                    payload = ObjectMapperCache.objectMapper.convertValue(event)
                )
                kafkaProducer.produce("order", message)
            }
            .doOnNext { log.info("Successfully publish event to topic order") }
            .doOnError { log.error("Failed to publish event to topic", it) }
            .then(Mono.fromCallable {
                order.events.removeAll { true }
                order
            })
    }
}