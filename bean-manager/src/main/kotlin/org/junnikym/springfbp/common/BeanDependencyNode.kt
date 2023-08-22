package org.junnikym.springfbp.common

import org.springframework.beans.factory.config.BeanDefinition

class BeanDependencyNode (name: String, bean: Any, definition: BeanDefinition) {

    val name: String = name;
    val bean: Any = bean;
    val definition : BeanDefinition = definition;

    override fun toString(): String {
        return name;
    }

}