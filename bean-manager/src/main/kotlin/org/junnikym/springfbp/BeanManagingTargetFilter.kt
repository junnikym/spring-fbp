package org.junnikym.springfbp

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class BeanManagingTargetFilter (
        @Value("\${spring.fbp.base-package}") private val basePackage: String
) {

    fun isManageTarget(beanClass: KClass<*>): Boolean = isManageTarget(beanClass.java)

    fun isManageTarget(beanClass: Class<*>): Boolean {
        return isInBasePackage(beanClass) && !isIgnoreManage(beanClass)
    }

    fun isInBasePackage(beanClassName: String): Boolean {
        return beanClassName.startsWith(basePackage)
    }

    fun isInBasePackage(beanClass: Class<*>): Boolean {
        return isInBasePackage(beanClass.`package`.name)
    }

    fun isIgnoreManage(beanClass: Class<*>): Boolean {
        return beanClass.annotations.any {
            when(it.annotationClass) {
                SpringBootApplication::class, IgnoreManage::class -> true
                else -> false
            }
        }
    }

}