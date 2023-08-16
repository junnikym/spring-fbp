package org.junnikym.springfbp

import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

abstract class AbstractBeanMonitoringService {

    private val publisher: Sinks.Many<BeanEvent> = Sinks.many().multicast().onBackpressureBuffer()

    fun getFlux(): Flux<BeanEvent> {
        return publisher.asFlux()
    }

    protected fun emit(event: BeanEvent) {
        publisher.tryEmitNext(event)
    }

}