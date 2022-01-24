package org.learning.demo.app.view

import org.learning.demo.app.domain.Product

data class CartView(
    val id: String,
    val products: Set<Product>
)