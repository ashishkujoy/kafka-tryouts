package org.learning.demo.app.service

import org.learning.demo.app.domain.Cart
import org.learning.demo.app.domain.Order
import org.learning.demo.app.repository.CartRepository
import org.learning.demo.app.view.CartView
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val orderService: OrderService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun checkout(cartId: String): Mono<Order> {
        return findCartBy(cartId)
            .flatMap { cart ->
                cart.markAsCheckedOut()
                cartRepository.save(cart)
            }
            .flatMap { createOrderFrom(it) }
            .doOnSuccess { logger.info("Successfully created order from cart") }
            .doOnError { logger.error("Failed to create order from cart", it) }
    }

    private fun findCartBy(cartId: String): Mono<Cart> {
        return cartRepository.findById(cartId)
            .doOnSuccess { logger.info("Successfully fetched cart for id $cartId") }
            .doOnError { logger.error("Failed to fetch cart for id $cartId", it) }
    }

    private fun createOrderFrom(cart: Cart): Mono<Order> {
        return orderService.createOrderFrom(CartView(cart.id, cart.products.toSet()))
    }
}