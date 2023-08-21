package org.junnikym.springfbp

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
@IgnoreManage
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
        if(beanClass.annotations.any(::isAnnotationForIgnore))
            return true

        if(getSuperClasses(beanClass).any { it.annotations.any(::isAnnotationForIgnore) })
            return true

        return beanClass.interfaces.any { it.annotations.any(::isAnnotationForIgnore) }
    }

    private fun isAnnotationForIgnore(annotation: Annotation): Boolean {
        return when(annotation.annotationClass) {
            SpringBootApplication::class, IgnoreManage::class -> true
            else -> false
        }
    }

    private fun getSuperClasses(cls: Class<*>): List<Class<*>> {
        val superClassList = mutableListOf<Class<*>>()
        var superClass = cls.superclass

        while(superClass != null) {
            superClassList.add(superClass)
            superClass = superClass.superclass
        }

        return superClassList
    }

}