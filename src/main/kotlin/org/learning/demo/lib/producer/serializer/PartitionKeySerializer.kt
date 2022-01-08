package org.learning.demo.lib.producer.serializer

import org.apache.kafka.common.serialization.Serializer
import org.learning.demo.lib.producer.PartitionKey

class PartitionKeySerializer : Serializer<PartitionKey> {
    override fun serialize(topic: String, data: PartitionKey): ByteArray {
        return ObjectMapperCache.objectMapper.writeValueAsBytes(data)
    }
}