package org.learning.demo.config

import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Configuration {
    @Bean
    fun mongoD(): MongodConfig {
        return MongodConfig.builder()
            .version(Version.Main.V4_0)
            .net(Net("localhost", 27018, Network.localhostIsIPv6()))
            .build()
    }
}