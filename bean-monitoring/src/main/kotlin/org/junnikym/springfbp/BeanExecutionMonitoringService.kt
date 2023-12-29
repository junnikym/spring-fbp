package org.junnikym.springfbp

import org.springframework.stereotype.Service
import java.lang.reflect.Method

@Service
class BeanExecutionMonitoringService: AbstractBeanMonitoringService () {

    fun execute(event: BeanEvent) {
        this.emit(event)
    }

    fun exit(event: BeanEvent) {
    }

}