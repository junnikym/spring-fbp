package org.junnikym.springfbp

interface BeanDependencyLinkFactory {

    fun add(link: BeanDependencyLink);

    fun add(link: List<BeanDependencyLink>);

    fun getLinks(): ArrayList<BeanDependencyLink>;

    fun getLinks(name: String): ArrayList<BeanDependencyLink>;

}