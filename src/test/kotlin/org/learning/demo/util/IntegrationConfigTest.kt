package org.learning.demo.util

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName


class IntegrationConfigTest : BeforeEachCallback {
    companion object {
        private val mongodbContainer = MongoDBContainer(DockerImageName.parse("mongo:4.0.10"))
        private val kafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"))

        class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
            override fun initialize(applicationContext: ConfigurableApplicationContext) {
                if (!mongodbContainer.isRunning) {
                    mongodbContainer.start()
                }
                if (!kafkaContainer.isRunning) {
                    kafkaContainer.start()
                }
                TestPropertyValues.of(
                    mapOf(
                        "spring.data.mongodb.uri" to mongodbContainer.replicaSetUrl,
                        "kafka.bootstrap-servers" to kafkaContainer.bootstrapServers
                    )
                ).applyTo(applicationContext.environment)
            }
        }
    }

    override fun beforeEach(context: ExtensionContext?) {
        if (!mongodbContainer.isRunning) {
            mongodbContainer.start()
        }
        if (!kafkaContainer.isRunning) {
            kafkaContainer.start()
        }
    }

}