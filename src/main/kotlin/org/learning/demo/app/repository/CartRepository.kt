package org.learning.demo.app.repository

import org.learning.demo.app.domain.Cart
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CartRepository : ReactiveCrudRepository<Cart, String>