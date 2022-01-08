package org.learning.demo.lib.producer.serializer

import org.apache.kafka.common.serialization.Serializer
import org.learning.demo.lib.producer.KafkaMessage

class KafkaMessageSerializer : Serializer<KafkaMessage> {
    override fun serialize(topic: String, data: KafkaMessage): ByteArray {
        return ObjectMapperCache.objectMapper.writeValueAsBytes(data)
    }
}