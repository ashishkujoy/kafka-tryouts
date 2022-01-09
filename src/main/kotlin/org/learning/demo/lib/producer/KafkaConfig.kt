package org.learning.demo.lib.producer

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("kafka")
data class KafkaConfig(val bootstrapServers: String)