package org.junnikym.springfbp

import org.junnikym.springfbp.filter.BeanManagingTargetFilter
import org.springframework.stereotype.Service
import java.lang.reflect.Method

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

        this.emit(event)
    }

    fun exit(method: Method) {
        val event = eventStorage.get()
        if(event?.from != null)
            eventStorage.set(event.from)
    }

}