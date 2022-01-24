package org.learning.demo.app.service

import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.learning.demo.app.domain.Cart
import org.learning.demo.app.domain.Order
import org.learning.demo.app.domain.Product
import org.learning.demo.app.repository.CartRepository
import org.learning.demo.util.assertNextWith
import reactor.core.publisher.Mono
import java.math.BigDecimal

class CartServiceTest {
    private val cartRepository = mockk<CartRepository>()
    private val orderService = mockk<OrderService>()
    private val products = listOf(Product("P-001", 2, BigDecimal("20")))
    private val cartService = CartService(cartRepository, orderService)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should create order for on cart checkout`() {
        val expectedOrder = Order(id = "O-11", products = products.toSet(), BigDecimal("40"))
        val cart = Cart("cid-001", products, false)

        every { orderService.createOrderFrom(any()) } returns Mono.just(expectedOrder)
        every { cartRepository.findById(any<String>()) } returns Mono.just(cart)
        every { cartRepository.save(any()) } returns Mono.just(cart)

        val result = cartService.checkout("cid-001")

        assertNextWith(result) { order ->
            order shouldBe expectedOrder
        }
    }

    @Test
    fun `should mark cart as checkout on cart checkout`() {
        val expectedOrder = Order(id = "O-11", products = products.toSet(), BigDecimal("40"))
        val cart = Cart("cid-001", products, false)

        every { orderService.createOrderFrom(any()) } returns Mono.just(expectedOrder)
        every { cartRepository.findById(any<String>()) } returns Mono.just(cart)
        every { cartRepository.save(any()) } returns Mono.just(cart)

        val result = cartService.checkout("cid-001")

        assertNextWith(result) {
            cart.isCheckout shouldBe true
        }
    }

    @Test
    fun `should save checkout cart cart`() {
        val expectedOrder = Order(id = "O-11", products = products.toSet(), BigDecimal("40"))
        val cart = Cart("cid-001", products, false)

        every { orderService.createOrderFrom(any()) } returns Mono.just(expectedOrder)
        every { cartRepository.findById(any<String>()) } returns Mono.just(cart)
        every { cartRepository.save(any()) } returns Mono.just(cart)

        val result = cartService.checkout("cid-001")

        assertNextWith(result) {
            verify(exactly = 1) { cartRepository.save(cart) }
        }
    }
}