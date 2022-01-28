package org.learning.demo.app.service

import com.fasterxml.jackson.module.kotlin.convertValue
import org.learning.demo.app.domain.Order
import org.learning.demo.app.domain.OrderIdGenerator
import org.learning.demo.app.domain.event.OrderCreatedEvent
import org.learning.demo.app.repository.OrderPersistorAndEventPublisher
import org.learning.demo.app.repository.OrderRepository
import org.learning.demo.app.view.CartView
import org.learning.demo.lib.kafka.KafkaMessage
import org.learning.demo.lib.kafka.producer.KafkaProducer
import org.learning.demo.lib.kafka.serializer.ObjectMapperCache
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kafka.sender.SenderResult
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.streams.toList

@Service
class OrderService(
    private val orderPersistorAndEventPublisher: OrderPersistorAndEventPublisher,
    private val orderIdGenerator: OrderIdGenerator,
) {

    fun createOrderFrom(cart: CartView): Mono<Order> {
        val order = Order(
            id = orderIdGenerator.generateNewId(),
            products = cart.products,
            totalPrice = cart.products.map { it.price * BigDecimal(it.quantity) }.reduce { sum, price -> sum + price }
        ).also { it.markAsCreated() }

        return orderPersistorAndEventPublisher.saveAndPublishEvents(order)
    }
}