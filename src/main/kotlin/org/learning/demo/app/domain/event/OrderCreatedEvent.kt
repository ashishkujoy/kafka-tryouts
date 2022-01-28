package org.learning.demo.app.domain.event

import org.learning.demo.app.domain.Order
import java.math.BigDecimal

data class OrderCreatedEvent(
    val orderId: String,
    val products: Set<ProductData>,
    val totalPrice: BigDecimal
) : DomainEvent {
    override val eventName: String = "order-created"

    companion object {
        fun from(order: Order): OrderCreatedEvent {
            return OrderCreatedEvent(
                orderId = order.id,
                products = order.products.map { ProductData(it.productCode, it.quantity, it.price) }.toSet(),
                totalPrice = order.totalPrice
            )
        }
    }
}

data class ProductData(val productCode: String, val quantity: Int, val price: BigDecimal)