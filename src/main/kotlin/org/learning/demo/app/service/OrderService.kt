package org.learning.demo.app.service

import org.learning.demo.app.domain.Order
import org.learning.demo.app.domain.OrderIdGenerator
import org.learning.demo.app.repository.OrderPersistorAndEventPublisher
import org.learning.demo.app.view.CartView
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal

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