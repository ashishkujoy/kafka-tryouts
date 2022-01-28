package org.learning.demo.app.domain

import org.learning.demo.app.domain.event.Aggregate
import org.learning.demo.app.domain.event.OrderCreatedEvent
import java.math.BigDecimal

data class Order(val id: String, val products: Set<Product>, val totalPrice: BigDecimal) : Aggregate() {
    fun markAsCreated() {
        addEvent(OrderCreatedEvent.from(this))
    }
}
