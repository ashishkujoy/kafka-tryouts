package org.learning.demo.lib.kafka.service

import org.learning.demo.lib.kafka.repository.ProcessedMessageAudit
import org.learning.demo.lib.kafka.repository.ProcessedMessageAuditRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.extra.retry.retryExponentialBackoff
import java.time.Duration
import java.time.LocalDateTime

@Service
class ProcessedMessageService(
    private val processedMessageAuditRepository: ProcessedMessageAuditRepository,
    private val consumerMessageProcessingAuditConfig: ConsumerMessageProcessingAuditConfig
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun isAlreadyProcessed(eventId: String): Mono<Boolean> {
        return processedMessageAuditRepository.findByEventId(eventId)
            .map { true }
            .switchIfEmpty(Mono.just(false))
            .doOnSuccess { log.info("Successfully checked if event having id $eventId is already processed or not") }
            .doOnError { log.error("Failed to check if event having id $eventId is already processed or not", it) }
    }

    fun markAsProcessed(eventId: String, consumerId: String): Mono<ProcessedMessageAudit> {
        val audit = ProcessedMessageAudit(eventId, LocalDateTime.now(), consumerId)

        return processedMessageAuditRepository.save(audit)
            .retryExponentialBackoff(
                times = consumerMessageProcessingAuditConfig.totalNumberOfRetry,
                first = Duration.ofMillis(consumerMessageProcessingAuditConfig.initialRetryDelayInMilliseconds),
            )
            .doOnSuccess { log.info("Successfully saved message processed audit for eventId: $eventId") }
            .doOnError { log.error("Failed to save message processed audit for eventId: $eventId", it) }
    }
}
