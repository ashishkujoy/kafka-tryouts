package org.learning.demo.app.repository

import org.learning.demo.app.domain.Order
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : ReactiveCrudRepository<Order, String>