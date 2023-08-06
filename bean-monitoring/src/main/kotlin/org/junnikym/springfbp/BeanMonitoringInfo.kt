package org.junnikym.springfbp

import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.stereotype.Controller
import java.lang.reflect.Method

class BeanMonitoringInfo(
        private val bean: Any,
        private val method: Method,
        private val from: Any?,
) {

    private companion object {
        val monitoringSet = mutableSetOf<BeanMonitoringInfo>()
    }

    init {
        monitoringSet.add(this)
    }

    fun exit() {
        monitoringSet.remove(this)
    }

    private fun isController(): Boolean {
        AnnotatedElementUtils.isAnnotated(this.bean.javaClass, Controller::class.java)
        return TODO()
    }

}