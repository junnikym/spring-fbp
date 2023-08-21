package org.junnikym.springfbp

@IgnoreManage
interface BeanDependencyNodeFactory {

    fun add(node: BeanDependencyNode)

    fun getAll(): Collection<BeanDependencyNode>

    fun getAllNames(): Collection<String>

    fun get(name: String): BeanDependencyNode?

}