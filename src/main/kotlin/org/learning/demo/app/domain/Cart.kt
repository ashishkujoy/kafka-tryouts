package org.learning.demo.app.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal

@Document("cart")
data class Cart(
    @Id
    val id: String,
    val products: List<Product>,
    var isCheckout: Boolean
) {
    fun totalProductPrice(): BigDecimal {
        return products
            .map { p -> p.price }
            .reduce { totalPrice, productPrice -> totalPrice + productPrice }
    }

    fun markAsCheckedOut() {
        this.isCheckout = true
    }
}
