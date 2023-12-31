package org.junnikym.springfbp

import org.junnikym.springfbp.filter.IgnoreManage
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

@IgnoreManage
abstract class AbstractBeanMonitoringService {

    private val eventMetastasisChannel: Sinks.Many<BeanEvent.Metastasis> = Sinks.many().multicast().onBackpressureBuffer()
    private val eventChannel: Sinks.Many<BeanEvent> = Sinks.many().multicast().onBackpressureBuffer()

    fun getEventMetastasisChannel(): Flux<BeanEvent.Metastasis> {
        return eventMetastasisChannel.asFlux()
    }

    protected fun emitEventMetastasis(event: BeanEvent) {
        eventMetastasisChannel.tryEmitNext(event.toMetastasis())
    }

    protected fun emitEvent(event: BeanEvent) {
        eventChannel.tryEmitNext(event)
    }

}