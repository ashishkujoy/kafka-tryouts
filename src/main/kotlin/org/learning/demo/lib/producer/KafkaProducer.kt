package org.learning.demo.lib.producer

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.learning.demo.lib.producer.serializer.KafkaMessageSerializer
import org.learning.demo.lib.producer.serializer.PartitionKeySerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import reactor.kafka.sender.SenderResult
import java.util.*
import javax.annotation.PostConstruct

@Component
class KafkaProducer {
    @Value("\${kafka.bootstrap-servers}")
    lateinit var kafkaServerAddress: String

    private lateinit var kafkaSender: KafkaSender<PartitionKey, KafkaMessage>

    @PostConstruct
    fun setUpKafkaSender() {
        kafkaSender = createKafkaSender()
    }

    fun produce(topic: String, message: KafkaMessage): Mono<SenderResult<String>> {
        val r = ProducerRecord(topic, PartitionKey(message.partitionKey), message)
        val senderRecord = SenderRecord.create(r, UUID.randomUUID().toString())
        return kafkaSender.send(Mono.just(senderRecord)).next()
    }

    private fun createKafkaSender(): KafkaSender<PartitionKey, KafkaMessage> {
        val props = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaServerAddress,
            ProducerConfig.CLIENT_ID_CONFIG to UUID.randomUUID().toString(),
            ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to 1,
            ProducerConfig.ACKS_CONFIG to "1",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to PartitionKeySerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to KafkaMessageSerializer::class.java
        )
        val senderOptions = SenderOptions.create<PartitionKey, KafkaMessage>(props)
        return KafkaSender.create(senderOptions)
    }
}

