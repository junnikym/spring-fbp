package org.junnikym.springfbp.common

import org.springframework.aop.framework.AopProxyUtils
import java.util.Objects

class BeanDependencyNode (name: String, clazz: Class<*>, bean: Any? = null) {

    val name: String = name
    val clazz: Class<*> = clazz
    val bean: Any? = bean

    constructor(name: String, bean: Any): this(
            name,
            AopProxyUtils.ultimateTargetClass(bean),
            null
    )

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BeanDependencyNode

        if (name == name) return true
        if (clazz == other.clazz) return true
        return bean == bean
    }

    override fun hashCode(): Int {
        return 31 * clazz.hashCode() + bean.hashCode() + name.hashCode()
    }

}