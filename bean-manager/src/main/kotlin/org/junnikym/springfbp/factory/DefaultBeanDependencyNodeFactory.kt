package org.junnikym.springfbp.factory

import org.junnikym.springfbp.common.BeanDependencyNode
import org.springframework.stereotype.Component

@Component
class DefaultBeanDependencyNodeFactory : BeanDependencyNodeFactory {

    private val nodeNameMap = mutableMapOf<String, BeanDependencyNode>()
    private val nodeClassMap = mutableMapOf<Class<*>, BeanDependencyNode>()

    override fun add(node: BeanDependencyNode) {
        nodeNameMap[node.name] = node
        nodeClassMap[node.clazz] = node
    }

    override fun getAll(): Collection<BeanDependencyNode> {
        return nodeNameMap.values
    }

    override fun getAllNames(): Collection<String> {
        return nodeNameMap.keys
    }

    override fun get(name: String): BeanDependencyNode? {
        return nodeNameMap[name]
    }

    override fun get(clazz: Class<*>): BeanDependencyNode? {
        return nodeClassMap[clazz]
    }

    override fun exists(name: String): Boolean {
        return get(name) != null
    }

    override fun exists(clazz: Class<*>): Boolean {
        return get(clazz) != null
    }

}