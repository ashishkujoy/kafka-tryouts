package org.learning.demo.util

import org.reactivestreams.Publisher
import reactor.test.StepVerifier

fun <T> assertNextWith(publisher: Publisher<T>, assertion: (T) -> Unit) {
    StepVerifier.create(publisher)
        .consumeNextWith(assertion)
        .verifyComplete()
}
