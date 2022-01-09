package org.learning.demo.lib.kafka.consumer

data class ConsumerConfig(
    val groupId: String,
    val consumerId: String,
    val readPreference: String,
)