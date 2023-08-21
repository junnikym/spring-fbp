package org.junnikym.springfbp

import org.springframework.stereotype.Component

@Component
class DefaultBeanDependencyNodeFactory : BeanDependencyNodeFactory {

    private val nodeMap = mutableMapOf<String, BeanDependencyNode>()

    override fun add(node: BeanDependencyNode) {
        nodeMap[node.name] = node
    }

    override fun getAll(): Collection<BeanDependencyNode> {
        return nodeMap.values
    }

    override fun getAllNames(): Collection<String> {
        return nodeMap.keys
    }

    override fun get(name: String): BeanDependencyNode? {
        return nodeMap[name]
    }

}