package org.junnikym.springfbp

import org.springframework.stereotype.Component

@Component
class DefaultBeanDependencyLinkFactory : BeanDependencyLinkFactory {

    // key : name of from (bean)
    private val linkMap : HashMap<String, ArrayList<BeanDependencyLink>> = HashMap();

    private val links : ArrayList<BeanDependencyLink> = ArrayList();

    override fun add(link: BeanDependencyLink) {
        val name = link.from.name;
        val linkList = linkMap.getOrDefault(name, ArrayList());
        linkList.add(link);
        linkMap[name] = linkList;
    }

    override fun add(link: List<BeanDependencyLink>) {
        link.forEach(::add);
    }

    override fun getLinks(): ArrayList<BeanDependencyLink> {
        return links;
    }

    override fun getLinks(name: String): ArrayList<BeanDependencyLink> {
        if(!linkMap.containsKey(name))
            throw RuntimeException("Not exists links");

        return linkMap[name]!!;
    }

    override fun toString(): String {
        return linkMap.toString()
    }

}