package org.learning.demo

import org.junit.jupiter.api.extension.ExtendWith
import org.learning.demo.util.IntegrationConfigTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
//@EmbeddedKafka(partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9092", "port=9092"])
@ActiveProfiles("test")
@ExtendWith(SpringExtension::class, IntegrationConfigTest::class)
@ContextConfiguration(initializers = [IntegrationConfigTest.Companion.Initializer::class])
annotation class IntegrationTest