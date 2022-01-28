package org.learning.demo.app.service

import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.learning.demo.app.domain.Order
import org.learning.demo.app.domain.OrderIdGenerator
import org.learning.demo.app.domain.Product
import org.learning.demo.app.repository.OrderPersistorAndEventPublisher
import org.learning.demo.app.view.CartView
import org.learning.demo.lib.kafka.producer.KafkaProducer
import org.learning.demo.util.assertNextWith
import reactor.core.publisher.Mono
import java.math.BigDecimal

class OrderServiceTest {
    private val orderIdGenerator = mockk<OrderIdGenerator>()
    private val orderPersistorAndEventPublisher = mockk<OrderPersistorAndEventPublisher>()
    private val orderService = OrderService(orderPersistorAndEventPublisher, orderIdGenerator)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should create order from given cart`() {
        val products = setOf(
            Product(
                productCode = "P-001", quantity = 2, price = BigDecimal("20")
            )
        )
        val cart = CartView(
            id = "C001",
            products = products
        )

        every { orderIdGenerator.generateNewId() } returns "001"
        every { orderPersistorAndEventPublisher.saveAndPublishEvents(any()) } answers { Mono.just(firstArg()) }

        assertNextWith(orderService.createOrderFrom(cart)) { order ->
            order shouldBe Order(
                id = "001",
                products = products,
                totalPrice = BigDecimal("40")
            )
        }
    }

    @Test
    fun `should save new order`() {
        val products = setOf(
            Product(
                productCode = "P-001", quantity = 2, price = BigDecimal("20")
            )
        )
        val cart = CartView(
            id = "C001",
            products = products
        )

        every { orderIdGenerator.generateNewId() } returns "001"
        every { orderPersistorAndEventPublisher.saveAndPublishEvents(any()) } answers { Mono.just(firstArg()) }

        assertNextWith(orderService.createOrderFrom(cart)) { order ->
            verify(exactly = 1) {
                orderPersistorAndEventPublisher.saveAndPublishEvents(order)
            }
        }
    }
}