package org.junnikym.springfbp.common

import org.springframework.aop.framework.AopProxyUtils
import java.util.Objects

data class BeanDependencyNode (
    val beanName: String?,
    val nodeName: String,
    val clazz: Class<*>,
    val bean: Any? = null
) {

    fun isManaged(): Boolean {
        return bean != null
    }

    override fun toString(): String {
        return """
            BeanDependencyNode {
                name: $nodeName,
                clazz: $clazz,
                bean: $bean
            }
        """.trimIndent()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BeanDependencyNode

        if (beanName != other.beanName) return false
        if (nodeName != other.nodeName) return false
        if (clazz != other.clazz) return false

        return bean == other.bean
    }

    override fun hashCode(): Int {
        return 31 * clazz.hashCode() + bean.hashCode() + beanName.hashCode() + nodeName.hashCode()
    }

}