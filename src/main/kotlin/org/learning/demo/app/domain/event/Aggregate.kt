package org.learning.demo.app.domain.event

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Transient
import java.util.*

open class Aggregate {
    @Transient
    @JsonIgnore
    val events: Queue<DomainEvent> = LinkedList()

    fun addEvent(event: DomainEvent) {
        events.add(event)
    }

    fun removeAllEvents() {
        events.removeAll { true }
    }
}