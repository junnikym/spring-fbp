package org.junnikym.springfbp

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Service

@Service
class BeanMonitoringTargetUtilService (
        @Value("\${spring.fbp.base-package}") private val basePackage: String
) {

    fun isMonitoringTarget(beanClass: Class<*>): Boolean {
        return isInBasePackage(beanClass) && !isIgnoreMonitoring(beanClass)
    }

    fun isInBasePackage(beanClassName: String): Boolean {
        return beanClassName.startsWith(basePackage)
    }

    fun isInBasePackage(beanClass: Class<*>): Boolean {
        return isInBasePackage(beanClass.`package`.name)
    }

    fun isIgnoreMonitoring(beanClass: Class<*>): Boolean {
        return beanClass.annotations.any {
            when(it.annotationClass) {
                SpringBootApplication::class, IgnoreMonitoring::class -> true
                else -> false
            }
        }
    }

}