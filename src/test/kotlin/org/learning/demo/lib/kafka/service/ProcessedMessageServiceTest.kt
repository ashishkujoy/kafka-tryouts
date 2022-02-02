package org.learning.demo.lib.kafka.service

import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.learning.demo.IntegrationTest
import org.learning.demo.lib.kafka.repository.ProcessedMessageAudit
import org.learning.demo.lib.kafka.repository.ProcessedMessageAuditRepository
import org.learning.demo.util.assertNextWith
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

@IntegrationTest
class ProcessedMessageAuditServiceIntegrationTest(
    @Autowired private val processedMessageAuditService: ProcessedMessageAuditService,
    @Autowired private val processedMessageAuditRepository: ProcessedMessageAuditRepository
) {

    @BeforeEach
    fun setUp() {
        processedMessageAuditRepository.deleteAll().block()
    }

    @Test
    fun `should save audit for processed message`() {
        processedMessageAuditService.markAsProcessed("E-001", "Con-001").block()

        assertNextWith(processedMessageAuditRepository.findByEventId("E-001")) {
            it.shouldBeEqualToIgnoringFields(
                ProcessedMessageAudit("E-001", LocalDateTime.now(), "Con-001"),
                ProcessedMessageAudit::processedAt
            )
        }
    }

    @Test
    fun `should return true if message is already processed`() {
        processedMessageAuditService.markAsProcessed("E-002", "Con-001").block()

        assertNextWith(processedMessageAuditService.isAlreadyProcessed("E-002")) {
            it shouldBe true
        }
    }

    @Test
    fun `should return false if message is not already processed`() {
        processedMessageAuditService.markAsProcessed("E-003", "Con-001").block()

        assertNextWith(processedMessageAuditService.isAlreadyProcessed("E-004")) {
            it shouldBe false
        }
    }
}
