package org.junnikym.springfbp

import org.junnikym.springfbp.filter.BeanManagingTargetFilter
import org.springframework.stereotype.Service
import java.lang.reflect.Method
import java.time.LocalDateTime

@Service
class BeanExecutionMonitoringService (
    private val beanManagingTargetFilter: BeanManagingTargetFilter
): AbstractBeanMonitoringService() {

    private val eventStorage = ThreadLocal<BeanEvent>()

    fun execute(bean: Any, method: Method) {
        if(!beanManagingTargetFilter.isManageTarget(method.declaringClass))
            return

        val lastEvent = eventStorage.get()
        val event = BeanEvent(
            bean = bean,
            method = method,
            from = lastEvent
        )

        lastEvent?.to?.add(event)
        eventStorage.set(event)
    }

    fun exit() {
        val event = eventStorage.get()
        if(event?.from != null)
            eventStorage.set(event.from)

        if(event?.to?.isEmpty() ?: false)
            this.emitEventMetastasis(event)


        event.finishedAt = LocalDateTime.now()
        this.emitEvent(event)
    }

}