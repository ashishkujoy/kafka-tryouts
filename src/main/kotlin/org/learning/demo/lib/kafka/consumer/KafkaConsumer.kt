package org.learning.demo.lib.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.learning.demo.lib.kafka.KafkaConfig
import org.learning.demo.lib.kafka.KafkaMessage
import org.learning.demo.lib.kafka.serializer.KafkaMessageDeserializer
import org.learning.demo.lib.kafka.serializer.ObjectMapperCache
import org.learning.demo.lib.kafka.serializer.PartitionKeyDeserializer
import org.learning.demo.lib.kafka.service.ProcessedMessageAuditService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.Disposable
import reactor.core.publisher.Mono
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.receiver.ReceiverRecord
import java.util.*
import javax.annotation.PostConstruct

abstract class KafkaConsumer(
    kafkaConfig: KafkaConfig,
    private val consumerConfig: ConsumerConfig,
    private val topicsToConsume: List<String>,
    private val additionalConsumerProps: Map<String, Any>,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private lateinit var consumerSubscription: Disposable
    private val consumerId = "${consumerConfig.consumerId}_${UUID.randomUUID()}"

    @Autowired
    private lateinit var processedMessageAuditService: ProcessedMessageAuditService

    private val commonConsumerProperties = mapOf<String, Any>(
        KEY_DESERIALIZER_CLASS_CONFIG to PartitionKeyDeserializer::class.java,
        VALUE_DESERIALIZER_CLASS_CONFIG to KafkaMessageDeserializer::class.java,
        ENABLE_AUTO_COMMIT_CONFIG to false,
        BOOTSTRAP_SERVERS_CONFIG to kafkaConfig.bootstrapServers,
        GROUP_ID_CONFIG to consumerId,
        GROUP_INSTANCE_ID_CONFIG to consumerConfig.groupId,
    )

    private fun getKafkaReceiver(): KafkaReceiver<String, KafkaMessage> {
        val allProps = commonConsumerProperties + additionalConsumerProps
        val receiverOptions = ReceiverOptions.create<String, KafkaMessage>(allProps)
            .subscription(topicsToConsume)
        return KafkaReceiver.create(receiverOptions)
    }

    abstract fun processMessage(
        topicName: String,
        headers: Map<String, String>,
        message: KafkaMessage,
        isNotProcessedVerified: Boolean,
    ): Mono<Any?>

    open fun errorHandler(
        receiverRecord: ReceiverRecord<String, KafkaMessage>,
        error: Throwable
    ): Mono<ReceiverRecord<String, KafkaMessage>> {
        return Mono.just(receiverRecord)
            .doOnSuccess {
                logger.error("No error handler configured, ignoring error.", error)
            }
    }

    @PostConstruct
    fun run() {
        consumerSubscription = getKafkaReceiver().receive()
            .flatMap { record ->
                processedMessageAuditService.isAlreadyProcessed(record.value().eventId, consumerConfig.groupId).map { isProcessed ->
                    Triple(isProcessed, true, record)
                }.onErrorResume {
                    Mono.just(Triple(false, false, record))
                }
            }
            .filter {
                !it.first
            }
            .flatMap { (_, isNotProcessedVerified, receiverRecord) ->
                val headers = receiverRecord.headers().associate { header ->
                    header.key() to ObjectMapperCache.objectMapper.readValue(header.value(), String::class.java)
                }
                processMessage(
                    receiverRecord.topic(),
                    headers,
                    receiverRecord.value(),
                    isNotProcessedVerified
                )
                    .map { receiverRecord }
                    .onErrorResume {
                        errorHandler(receiverRecord, it)
                    }
            }
            .doOnNext {
                logger.info("Message successfully process message from topic {}", it.topic())
            }
            .flatMap { record ->
                record.receiverOffset().commit().map { record }.switchIfEmpty(Mono.just(record))
            }
            .flatMap {
                processedMessageAuditService.markAsProcessed(
                    eventId = it.value().eventId,
                    consumerGroupId = consumerConfig.groupId
                ).map { true }
            }
            .doOnError { error ->
                logger.error("Encounter error in kafka consumer", error)
            }
            .onErrorResume { Mono.just(true) }
            .subscribe()
    }

    fun stop() {
        if (this::consumerSubscription.isInitialized) {
            this.consumerSubscription.dispose()
        }
    }
}