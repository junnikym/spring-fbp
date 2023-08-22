package org.junnikym.springfbp.factory

import org.junnikym.springfbp.common.BeanDependencyNode
import org.junnikym.springfbp.filter.IgnoreManage

@IgnoreManage
interface BeanDependencyNodeFactory {

    fun add(node: BeanDependencyNode)

    fun getAll(): Collection<BeanDependencyNode>

    fun getAllNames(): Collection<String>

    fun get(name: String): BeanDependencyNode?

}