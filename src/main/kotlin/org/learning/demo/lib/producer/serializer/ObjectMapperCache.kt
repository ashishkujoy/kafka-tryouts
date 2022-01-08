package org.learning.demo.lib.producer.serializer

import com.fasterxml.jackson.databind.ObjectMapper

object ObjectMapperCache {
    val objectMapper = ObjectMapper()
}