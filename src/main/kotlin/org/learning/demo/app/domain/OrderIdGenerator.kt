package org.learning.demo.app.domain

import org.springframework.stereotype.Component
import java.util.*

@Component
class OrderIdGenerator {
    fun generateNewId(): String {
        return UUID.randomUUID().toString()
    }
}