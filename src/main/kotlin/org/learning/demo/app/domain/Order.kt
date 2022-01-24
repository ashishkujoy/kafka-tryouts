package org.learning.demo.app.domain

import java.math.BigDecimal

data class Order(val id: String, val products: Set<Product>, val totalPrice: BigDecimal)
