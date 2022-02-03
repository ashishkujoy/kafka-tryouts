package org.learning.demo.app.consumer

import io.kotest.assertions.timing.EventuallyConfig
import io.kotest.assertions.timing.continually
import io.kotest.assertions.timing.eventually
import io.kotest.assertions.until.FixedInterval
import io.kotest.assertions.until.fixed
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.learning.demo.IntegrationTest
import org.learning.demo.app.repository.InventoryProduct
import org.learning.demo.app.repository.ProductRepository
import org.learning.demo.lib.kafka.KafkaMessage
import org.learning.demo.lib.kafka.producer.KafkaProducer
import org.learning.demo.lib.kafka.repository.ProcessedMessageAudit
import org.learning.demo.lib.kafka.repository.ProcessedMessageAuditRepository
import org.learning.demo.util.assertNextWith
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@IntegrationTest
class InventoryConsumerTest(
    @Autowired private val kafkaProducer: KafkaProducer,
    @Autowired private val productRepository: ProductRepository,
    @Autowired private val processedMessageAuditRepository: ProcessedMessageAuditRepository,
) {

    @BeforeEach
    fun setUp() {
        processedMessageAuditRepository.deleteAll().block()
    }

    @Test
    fun `should update count of remain products on consuming order created event`() {
        productRepository.saveAll(
            listOf(
                InventoryProduct("P-001", 20),
                InventoryProduct("P-002", 12),
                InventoryProduct("P-003", 12),
            )
        ).blockLast()

        kafkaProducer.produce(
            "order",
            KafkaMessage(
                eventId = "ev-00-11",
                eventName = "order-created",
                createdTime = "2020-01-01",
                partitionKey = "123",
                identifiers = mapOf(),
                payload = mapOf(
                    "orderId" to "001",
                    "totalPrice" to 234.56,
                    "products" to setOf(
                        mapOf(
                            "productCode" to "P-001",
                            "quantity" to 12,
                            "price" to 345.32
                        ),
                        mapOf(
                            "productCode" to "P-002",
                            "quantity" to 12,
                            "price" to 345.32
                        ),
                    )
                )
            )
        ).block()

        runBlocking {
            eventually(EventuallyConfig(
                duration = 5.toDuration(DurationUnit.SECONDS),
                interval = FixedInterval(100.toDuration(DurationUnit.MILLISECONDS))
            )) {
                assertNextWith(productRepository.findByProductCode("P-001")) {
                    it.remainingQuantity shouldBe 8
                }
                assertNextWith(productRepository.findByProductCode("P-002")) {
                    it.remainingQuantity shouldBe 0
                }
                assertNextWith(productRepository.findByProductCode("P-003")) {
                    it.remainingQuantity shouldBe 12
                }
            }
        }

    }

    @Test
    fun `should not process duplicate message`() {
        processedMessageAuditRepository.save(ProcessedMessageAudit(
            eventId = "001_order-created",
            processedAt = LocalDateTime.now(),
            consumerGroupId = "inventory-consumer-group"
        )).block()

        productRepository.saveAll(
            listOf(
                InventoryProduct("P-001", 20),
                InventoryProduct("P-002", 12),
                InventoryProduct("P-003", 12),
            )
        ).blockLast()

        kafkaProducer.produce(
            "order",
            KafkaMessage(
                eventId = "ev-00-11",
                eventName = "order-created",
                createdTime = "2020-01-01",
                partitionKey = "123",
                identifiers = mapOf(),
                payload = mapOf(
                    "orderId" to "001",
                    "totalPrice" to 234.56,
                    "products" to setOf(
                        mapOf(
                            "productCode" to "P-001",
                            "quantity" to 12,
                            "price" to 345.32
                        ),
                        mapOf(
                            "productCode" to "P-002",
                            "quantity" to 12,
                            "price" to 345.32
                        ),
                    )
                )
            )
        ).block()

        runBlocking {
            continually(5.seconds, 1.seconds.fixed()) {
                assertNextWith(productRepository.findByProductCode("P-001")) {
                    it.remainingQuantity shouldBe 20
                }
                assertNextWith(productRepository.findByProductCode("P-002")) {
                    it.remainingQuantity shouldBe 12
                }
                assertNextWith(productRepository.findByProductCode("P-003")) {
                    it.remainingQuantity shouldBe 12
                }
            }
        }

    }
}