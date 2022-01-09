package org.learning.demo.lib.producer.serializer

import org.apache.kafka.common.serialization.Deserializer
import org.learning.demo.lib.producer.PartitionKey

class PartitionKeyDeserializer : Deserializer<PartitionKey> {
    override fun deserialize(topic: String, data: ByteArray): PartitionKey {
        return ObjectMapperCache.objectMapper.readValue(data, PartitionKey::class.java)
    }
}