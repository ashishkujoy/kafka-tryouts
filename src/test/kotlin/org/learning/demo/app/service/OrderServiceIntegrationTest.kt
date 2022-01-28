package org.learning.demo.app.service

import io.kotest.assertions.timing.EventuallyConfig
import io.kotest.assertions.timing.eventually
import io.kotest.assertions.until.FixedInterval
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.learning.demo.IntegrationTest
import org.learning.demo.app.domain.Product
import org.learning.demo.app.view.CartView
import org.learning.demo.util.EmbeddedKafkaConsumer
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@IntegrationTest
class OrderServiceIntegrationTest(
    @Autowired private val orderService: OrderService,
    @Autowired private val embeddedKafkaConsumer: EmbeddedKafkaConsumer
) {

    @BeforeEach
    fun setUp() {
        embeddedKafkaConsumer.clearTopic("order")
    }

    @Test
    fun `should raise order created event on order creation`() {
        val product = Product(productCode = "P-0022", quantity = 3, price = BigDecimal("20.34"))

        orderService.createOrderFrom(CartView(id = "C-002", products = setOf(product))).block()

        runBlocking {
            eventually(
                EventuallyConfig(
                    duration = 5.toDuration(DurationUnit.SECONDS),
                    interval = FixedInterval(1.toDuration(DurationUnit.SECONDS))
                )
            ) {
                val messages = embeddedKafkaConsumer.readNextMessage("order", "order-created")

                messages shouldHaveSize 1
                messages.first().payload shouldContainAll mapOf(
                    "products" to listOf(mapOf(
                        "productCode" to "P-0022",
                        "quantity" to 3,
                        "price" to 20.34,
                    )),
                    "totalPrice" to 61.02,
                    "eventName" to "order-created"
                )
            }
        }
    }
}