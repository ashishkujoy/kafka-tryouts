package org.learning.demo.lib.producer.serializer

import org.apache.kafka.common.serialization.Deserializer
import org.learning.demo.lib.producer.KafkaMessage

class KafkaMessageDeserializer : Deserializer<KafkaMessage> {
    override fun deserialize(topic: String?, data: ByteArray?): KafkaMessage {
        return ObjectMapperCache.objectMapper.readValue(data, KafkaMessage::class.java)
    }
}