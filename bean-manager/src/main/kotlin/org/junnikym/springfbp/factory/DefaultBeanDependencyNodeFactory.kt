package org.junnikym.springfbp.factory

import org.junnikym.springfbp.common.BeanDependencyNode
import org.springframework.stereotype.Component

@Component
class DefaultBeanDependencyNodeFactory : BeanDependencyNodeFactory {

    private val nodeNameMap = mutableMapOf<String, BeanDependencyNode>()
    private val beanNameMap = mutableMapOf<String, BeanDependencyNode>()
    private val nodeClassMap = mutableMapOf<Class<*>, BeanDependencyNode>()

    override fun add(node: BeanDependencyNode) {
        nodeNameMap[node.nodeName] = node
        nodeClassMap[node.clazz] = node

        node.beanName?.let { beanName-> beanNameMap.put(beanName, node) }
    }

    override fun getAll(): Collection<BeanDependencyNode> {
        return nodeNameMap.values
    }

    override fun getAllNames(): Collection<String> {
        return nodeNameMap.keys
    }

    override fun get(name: String): BeanDependencyNode? {
        return nodeNameMap[name] ?: beanNameMap[name]
    }

    override fun get(clazz: Class<*>): BeanDependencyNode? {
        val node = nodeClassMap[clazz]
        if(node != null)
            return node

        return clazz.interfaces
            .find(nodeClassMap::containsKey)
            ?.let(nodeClassMap::get)
    }

    override fun exists(name: String): Boolean {
        return get(name) != null
    }

    override fun exists(clazz: Class<*>): Boolean {
        return get(clazz) != null
    }

}