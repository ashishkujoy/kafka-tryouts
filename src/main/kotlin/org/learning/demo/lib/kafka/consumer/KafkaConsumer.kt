package org.learning.demo.lib.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.common.header.Headers
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

/**
 *  Base class which handles kafka connection and do all heave lifting of making sure filtering out duplicate/already
 *  processed messages.
 * */
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

    /**
     *  Function which return the unique id for this message.
     *  By default, eventId is used as unique id, override this method in business consumer if you want some other field
     *  or logic for determining unique id of message.
     * */

    open fun getUniqueIdFor(topicName: String, message: KafkaMessage, headers: Map<String, String>): String {
        return message.eventId
    }

    /**
     *  A business consumer class should implement this method.
     *  If the return Mono complete with any element we commit the record offset in kafka, on error we call errorHandler
     *  function.
     *
     *  @param topicName topic from which message is received.
     *  @param headers kafka message headers
     *  @param message kafka message
     *  @param isNotProcessedVerified true when it is verified that this message is not already processed by consumer,
     *         false if for any reason we were not verified that this message is already processed or not.
     *         when value is false business consumer should make it own decision to process this message or not.
     *
     *  @return a mono of any.
     * */
    abstract fun processMessage(
        topicName: String,
        headers: Map<String, String>,
        message: KafkaMessage,
        isNotProcessedVerified: Boolean,
    ): Mono<Any?>

    /**
     * Any error occur while process message will be handled here. By default, error will be logged and offset will be
     * committed, in case you want some other handling just override this method in your business consumer.
     *
     * @param receiverRecord the original record received.
     * @param error the error which has happened during processing
     * */

    open fun errorHandler(
        receiverRecord: ReceiverRecord<String, KafkaMessage>,
        error: Throwable
    ): Mono<ReceiverRecord<String, KafkaMessage>> {
        return Mono.just(receiverRecord)
            .doOnSuccess {
                logger.error("No error handler configured, ignoring error.", error)
            }
    }

    private fun headersAsMap(headers: Headers): Map<String, String> {
        return headers.associate { header ->
            header.key() to ObjectMapperCache.objectMapper.readValue(header.value(), String::class.java)
        }
    }

    @PostConstruct
    fun run() {
        consumerSubscription = getKafkaReceiver().receive()
            .flatMap { record ->
                processedMessageAuditService.isAlreadyProcessed(
                    getUniqueIdFor(record.topic(), record.value(), headersAsMap(record.headers())),
                    consumerConfig.groupId
                ).map { isProcessed ->
                    Triple(isProcessed, true, record)
                }.onErrorResume {
                    Mono.just(Triple(false, false, record))
                }
            }
            .flatMap { (isAlreadyProcessed, isNotProcessedVerified, receiverRecord) ->
                if(isAlreadyProcessed) {
                    Mono.just(receiverRecord)
                } else {
                    processMessage(
                        receiverRecord.topic(),
                        headersAsMap(receiverRecord.headers()),
                        receiverRecord.value(),
                        isNotProcessedVerified
                    )
                        .map { receiverRecord }
                        .onErrorResume {
                            errorHandler(receiverRecord, it)
                        }
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