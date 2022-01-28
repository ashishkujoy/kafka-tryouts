package org.learning.demo.lib.kafka.producer

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.learning.demo.lib.kafka.KafkaConfig
import org.learning.demo.lib.kafka.KafkaMessage
import org.learning.demo.lib.kafka.PartitionKey
import org.learning.demo.lib.kafka.serializer.KafkaMessageSerializer
import org.learning.demo.lib.kafka.serializer.PartitionKeySerializer
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import reactor.kafka.sender.SenderResult
import java.util.*
import javax.annotation.PostConstruct

@Component
class KafkaProducer(private val kafkaConfig: KafkaConfig) {

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
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaConfig.bootstrapServers,
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

