package org.learning.demo.lib.kafka.repository

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface ProcessedMessageAuditRepository : ReactiveCrudRepository<ProcessedMessageAudit, String> {
    fun findByEventIdAndConsumerGroupId(eventId: String, consumerGroupId: String): Mono<ProcessedMessageAudit>
}

@Document("processedMessageAudit")
data class ProcessedMessageAudit(
    @Id
    val eventId: String,
    val processedAt: LocalDateTime,
    val consumerGroupId: String
)