package org.learning.demo.app.service

import org.learning.demo.app.domain.event.OrderCreatedEvent
import org.learning.demo.app.domain.event.ProductData
import org.learning.demo.app.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class InventoryService(private val productRepository: ProductRepository) {

    @Transactional
    fun processOrderCreatedEvent(orderCreatedEvent: OrderCreatedEvent): Mono<OrderCreatedEvent> {
        return Flux.fromIterable(orderCreatedEvent.products)
            .flatMap(::updateRemainProductCount)
            .then(Mono.just(orderCreatedEvent))
    }

    private fun updateRemainProductCount(productData: ProductData) =
        productRepository.findByProductCode(productData.productCode)
            .flatMap { product ->
                product.remainingQuantity -= productData.quantity
                productRepository.save(product)
            }
}