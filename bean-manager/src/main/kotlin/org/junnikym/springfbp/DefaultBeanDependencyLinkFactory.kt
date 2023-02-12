package org.junnikym.springfbp

import org.springframework.stereotype.Component

@Component
class DefaultBeanDependencyLinkFactory : BeanDependencyLinkFactory {

    // key : name of from (bean); parent node
    // val : child node
    private val linkMap : HashMap<String, ArrayList<BeanDependencyLink>> = HashMap();

    // key : name of from (bean); child node
    // val : parent node
    private val reverseLinkMap : HashMap<String, ArrayList<BeanDependencyLink>> = HashMap();

    private val links : ArrayList<BeanDependencyLink> = ArrayList();

    private val rootNodeNames : HashSet<String> = HashSet();

    override fun add(link: BeanDependencyLink) {
        addToLinkMap(link);
        addToReverseLinkMap(link);
        updateRootNodeNames(link);
    }

    override fun add(link: List<BeanDependencyLink>) {
        link.forEach(::add);
    }

    private fun addToLinkMap(link: BeanDependencyLink) {
        val name = link.from.name;
        addToMap(name, link, linkMap);
    }

    private fun addToReverseLinkMap(link: BeanDependencyLink) {
        val name = link.to.name;
        addToMap(name, link, reverseLinkMap);
    }

    private fun addToMap(
        name: String,
        link: BeanDependencyLink,
        map: HashMap<String, ArrayList<BeanDependencyLink>>
    ) {
        val linkList = linkMap.getOrDefault(name, ArrayList());
        linkList.add(link);
        map[name] = linkList;
    }

    private fun updateRootNodeNames(link: BeanDependencyLink) {
        rootNodeNames
            .filter(::hasParent)
            .forEach(rootNodeNames::remove);

        // add new node (or not);
        if(!hasFromNodeParent(link))
            rootNodeNames.add(link.from.name);
    }



    override fun hasParent(nodeName: String): Boolean {
        return reverseLinkMap.containsKey(nodeName);
    }

    override fun hasFromNodeParent(link: BeanDependencyLink): Boolean {
        return reverseLinkMap.containsKey(link.from.name);
    }

    override fun isRoot(beanName: String): Boolean {
        return rootNodeNames.contains(beanName);
    }

    override fun getRootLinks(): List<BeanDependencyLink> {
        return rootNodeNames
            .filter(linkMap::containsKey)
            .map { linkMap.getOrDefault(it, ArrayList()) }
            .flatten();
    }

    override fun getRootNames(): List<String> {
        return rootNodeNames.map { it };
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