package org.learning.demo.lib.kafka

data class KafkaMessage(
    val eventId: String,
    val eventName: String,
    val createdTime: String,
    val partitionKey: String,
    val identifiers: Map<String, Any>,
    val payload: Map<String, Any>,
)