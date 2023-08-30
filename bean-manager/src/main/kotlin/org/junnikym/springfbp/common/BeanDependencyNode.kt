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

}