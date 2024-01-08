package org.junnikym.springfbp

import org.junnikym.springfbp.filter.IgnoreManage
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@IgnoreManage
class DependencyFlowEventController (
        private val beanExecutionMonitoringService: BeanExecutionMonitoringService,
) {

    @GetMapping(path = ["/api/v1/event/flow/execution"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribeExecutionEvent(): Flux<BeanEvent.Metastasis> {
        return beanExecutionMonitoringService.getEventMetastasisChannel();
    }

}