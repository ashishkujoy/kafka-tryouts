package org.learning.demo

import org.junit.jupiter.api.extension.ExtendWith
import org.learning.demo.util.IntegrationTestExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(SpringExtension::class, IntegrationTestExtension::class)
@ContextConfiguration(initializers = [IntegrationTestExtension.Companion.Initializer::class])
annotation class IntegrationTest