package org.learning.demo.lib.kafka.service

import org.learning.demo.lib.kafka.repository.ProcessedMessageAudit
import org.learning.demo.lib.kafka.repository.ProcessedMessageAuditRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration
import java.time.LocalDateTime

@Service
class ProcessedMessageAuditService(
    private val processedMessageAuditRepository: ProcessedMessageAuditRepository,
    private val consumerMessageProcessingAuditConfig: ConsumerMessageProcessingAuditConfig
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun isAlreadyProcessed(eventId: String, consumerGroupId: String): Mono<Boolean> {
        return processedMessageAuditRepository.findByEventIdAndConsumerGroupId(eventId, consumerGroupId)
            .map { true }
            .switchIfEmpty(Mono.just(false))
            .doOnSuccess { log.info("Successfully checked if event having id $eventId is already processed or not") }
            .doOnError { log.error("Failed to check if event having id $eventId is already processed or not", it) }
    }

    fun markAsProcessed(eventId: String, consumerGroupId: String): Mono<ProcessedMessageAudit> {
        val audit = ProcessedMessageAudit(eventId, LocalDateTime.now(), consumerGroupId)

        return processedMessageAuditRepository.save(audit)
            .retryWhen(
                Retry.backoff(
                    consumerMessageProcessingAuditConfig.totalNumberOfRetry,
                    Duration.ofMillis(consumerMessageProcessingAuditConfig.initialRetryDelayInMilliseconds)
                )
            )
            .doOnSuccess { log.info("Successfully saved message processed audit for eventId: $eventId") }
            .doOnError { log.error("Failed to save message processed audit for eventId: $eventId", it) }
    }
}
