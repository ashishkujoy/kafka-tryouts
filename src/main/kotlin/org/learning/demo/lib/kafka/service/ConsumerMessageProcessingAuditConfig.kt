package org.learning.demo.lib.kafka.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("kafka.consumer.message-process-audit-config")
data class ConsumerMessageProcessingAuditConfig(
    val totalNumberOfRetry: Long,
    val initialRetryDelayInMilliseconds: Long
)