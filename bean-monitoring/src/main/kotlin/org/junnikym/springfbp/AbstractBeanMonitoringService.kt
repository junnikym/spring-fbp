package org.junnikym.springfbp

import org.junnikym.springfbp.filter.IgnoreManage
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

@IgnoreManage
abstract class AbstractBeanMonitoringService {

    private val publisher: Sinks.Many<BeanEvent> = Sinks.many().multicast().onBackpressureBuffer()

    fun getFlux(): Flux<BeanEvent> {
        return publisher.asFlux()
    }

    protected fun emit(event: BeanEvent) {
        publisher.tryEmitNext(event)
    }

}