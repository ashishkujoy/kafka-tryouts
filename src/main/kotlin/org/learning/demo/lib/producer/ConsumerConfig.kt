package org.learning.demo.lib.producer

data class ConsumerConfig(
    val groupId: String,
    val consumerId: String,
    val readPreference: String,
)