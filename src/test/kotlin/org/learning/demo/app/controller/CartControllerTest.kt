package org.learning.demo.app.controller

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.learning.demo.IntegrationTest
import org.learning.demo.app.domain.Cart
import org.learning.demo.app.domain.Order
import org.learning.demo.app.domain.Product
import org.learning.demo.app.repository.CartRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import java.math.BigDecimal

@IntegrationTest
class CartControllerTest(
    @Autowired private val webTestClient: WebTestClient,
    @Autowired private val cartRepository: CartRepository,
) {

    @BeforeEach
    fun setUp() {
        cartRepository.deleteAll().block()
    }

    @Test
    fun `should create an order when user do a cart checkout`() {
        val products = listOf(
            Product(
                productCode = "P-001",
                quantity = 1,
                price = BigDecimal("70")
            )
        )
        val cart = Cart(
            id = "C001",
            products = products,
            isCheckout = false
        )

        cartRepository.save(cart).block()

        val order = webTestClient
            .post()
            .uri("/api/v1/cart/C001/checkout")
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody(Order::class.java)
            .returnResult().responseBody

        assertSoftly {
            order.shouldNotBeNull()
            order.shouldBeEqualToIgnoringFields(
                Order(id = "C001", products = products.toSet(), totalPrice = BigDecimal("70")),
                Order::id,
            )
        }
    }
}