package org.junnikym.springfbp

interface BeanDependencyLinkFactory {

    fun add(link: BeanDependencyLink);

    fun add(link: List<BeanDependencyLink>);



    fun hasParent(nodeName: String): Boolean;

    fun hasFromNodeParent(link: BeanDependencyLink): Boolean;

    fun getParentNames(beanName: String): List<String>

    fun getLinksWithParent(beanName: String): List<BeanDependencyLink>



    fun isRoot(beanName: String): Boolean;

    fun getRootLinks(): List<BeanDependencyLink>;

    fun getRootNames(): List<String>;



    fun getLinks(): ArrayList<BeanDependencyLink>;

    fun getLinks(name: String): ArrayList<BeanDependencyLink>;

}