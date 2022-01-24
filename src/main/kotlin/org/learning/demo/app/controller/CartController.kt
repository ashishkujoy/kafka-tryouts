package org.learning.demo.app.controller

import org.learning.demo.app.domain.Order
import org.learning.demo.app.service.CartService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/cart")
class CartController(private val cartService: CartService) {

    @PostMapping("/{cartId}/checkout")
    fun checkout(@PathVariable cartId: String): Mono<Order> {
        return cartService.checkout(cartId)
    }
}