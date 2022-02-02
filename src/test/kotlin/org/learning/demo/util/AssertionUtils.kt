package org.learning.demo.util

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.reactivestreams.Publisher
import reactor.test.StepVerifier
import java.time.Clock
import java.time.ZoneId
import java.time.temporal.Temporal
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.jvm.javaType

fun <T> assertNextWith(publisher: Publisher<T>, assertion: (T) -> Unit) {
    StepVerifier.create(publisher)
        .consumeNextWith(assertion)
        .verifyComplete()
}

inline fun <T, reified Time : Temporal> withConstantNow(now: Time, block: () -> T): T {
    mockNow(now, Time::class)
    try {
        return block()
    } finally {
        unmockNow(Time::class)
    }
}

@PublishedApi
internal fun <Time : Temporal> mockNow(value: Time, klass: KClass<Time>) {
    mockkStatic(klass)

    getNowFunctions(klass).forEach {
        if(it.parameters.isEmpty()) {
            every { it.call() } returns value
        } else {
            if(it.parameters[0].type.javaType == ZoneId::class.java) {
                every { it.call(any<ZoneId>()) } returns value
            } else {
                every { it.call(any<Clock>()) } returns value
            }
        }
    }
}

internal fun <Time : Temporal> getNowFunctions(klass: KClass<in Time>): List<KFunction<*>> {
    return klass.staticFunctions.filter { it.name == "now" }
}

@PublishedApi
internal fun <Time : Temporal> unmockNow(klass: KClass<Time>) {
    unmockkStatic(klass)
}
