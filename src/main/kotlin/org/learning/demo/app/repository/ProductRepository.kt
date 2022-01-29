package org.learning.demo.app.repository

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ProductRepository : ReactiveCrudRepository<InventoryProduct, String> {
    fun findByProductCode(productCode: String): Mono<InventoryProduct>
}

@Document("product")
data class InventoryProduct(
    @Id
    val productCode: String,
    var remainingQuantity: Int
)