package org.junnikym.springfbp

import org.springframework.stereotype.Service
import java.lang.reflect.Method

@Service
class BeanExecutionMonitoringService: AbstractBeanMonitoringService () {

    private val monitoringMap = mutableMapOf<EventKey, MutableSet<BeanEvent>>()

    @Synchronized fun execute(event: BeanEvent) {
        val key = EventKey(event)
        val set = monitoringMap[key] ?: mutableSetOf(event)
        monitoringMap[key] = set

        this.emit(event)
    }

    @Synchronized fun exit(info: BeanEvent) {
        val key = EventKey(info)
        monitoringMap[key]?.remove(info)
    }

    private class EventKey(event: BeanEvent) {

        private val bean: Any = event.bean
        private val method: Method = event.method

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EventKey

            if (bean != other.bean) return false
            return method == other.method
        }

        override fun hashCode(): Int {
            return 31 * bean.hashCode() + method.hashCode()
        }

        override fun toString(): String {
            return "${bean::class.qualifiedName}::${method.name}"
        }

    }

}