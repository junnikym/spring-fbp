package org.junnikym.springfbp.factory

import org.junnikym.springfbp.common.BeanDependencyLink
import org.junnikym.springfbp.filter.IgnoreManage

@IgnoreManage
interface BeanDependencyLinkFactory {

    fun add(link: BeanDependencyLink);

    fun add(link: List<BeanDependencyLink>);



    fun hasParent(nodeName: String): Boolean;

    fun hasParentOfFromNode(link: BeanDependencyLink): Boolean;

    fun getParentNames(beanName: String): Collection<String>

    fun getLinksWithParent(beanName: String): Collection<BeanDependencyLink>



    fun isRoot(beanName: String): Boolean;

    fun getRootLinks(): Collection<BeanDependencyLink>;

    fun getRootNames(): Collection<String>;



    fun getLinks(): Collection<BeanDependencyLink>;

    fun getLinks(name: String): Collection<BeanDependencyLink>;

    fun getLinks(clazz: Class<*>): Collection<BeanDependencyLink>;

    fun getLinkedClasses(name: String): Collection<Class<*>>

    fun getLinkedClasses(clazz: Class<*>): Collection<Class<*>>

    fun isLinked(from: String, to: String): Boolean

    fun isLinked(from: Class<*>, to: Class<*>): Boolean



    fun getLinkedBeanNames(): Set<String>;

}