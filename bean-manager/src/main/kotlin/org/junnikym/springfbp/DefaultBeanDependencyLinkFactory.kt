package org.junnikym.springfbp

import org.springframework.stereotype.Component

@Component
class DefaultBeanDependencyLinkFactory : BeanDependencyLinkFactory {

    // key : name of from (bean); parent node
    // val : child node
    private val linkMap : HashMap<String, ArrayList<BeanDependencyLink>> = HashMap()

    // key : name of from (bean); child node
    // val : parent node
    private val reverseLinkMap : HashMap<String, ArrayList<BeanDependencyLink>> = HashMap()

    private val linkList : ArrayList<BeanDependencyLink> = ArrayList()

    private val rootNodeNameSet : HashSet<String> = HashSet()

    private val linkedBeanNameList : HashSet<String> = HashSet()

    override fun add(link: BeanDependencyLink) {
        addToListedBeanList(link)
        addToLinkList(link)
        addToLinkMap(link)
        addToReverseLinkMap(link)
        updateRootNodeNames(link)
    }

    override fun add(link: List<BeanDependencyLink>) {
        link.forEach(::add)
    }

    private fun addToListedBeanList(link: BeanDependencyLink) {
        linkedBeanNameList.add(link.from.name)
        linkedBeanNameList.add(link.to.name)
    }

    private fun addToLinkList(link: BeanDependencyLink) {
        linkList.add(link)
    }

    private fun addToLinkMap(link: BeanDependencyLink) {
        val name = link.to.name
        addToMap(name, link, linkMap)
    }

    private fun addToReverseLinkMap(link: BeanDependencyLink) {
        val name = link.from.name
        addToMap(name, link, reverseLinkMap)
    }

    private fun addToMap(
        name: String,
        link: BeanDependencyLink,
        map: HashMap<String, ArrayList<BeanDependencyLink>>
    ) {
        val linkList = map.getOrDefault(name, ArrayList())
        linkList.add(link)
        map[name] = linkList
    }

    private fun updateRootNodeNames(link: BeanDependencyLink) {
        rootNodeNameSet
            .filter(::hasParent)
            .forEach(rootNodeNameSet::remove)

        // add new node (or not);
        if(!hasParentOfFromNode(link))
            rootNodeNameSet.add(link.from.name)
    }



    override fun hasParent(nodeName: String): Boolean {
        return reverseLinkMap.containsKey(nodeName)
    }

    override fun hasParentOfFromNode(link: BeanDependencyLink): Boolean {
        return reverseLinkMap.containsKey(link.from.name)
    }

    override fun getParentNames(beanName: String): List<String> {
        return getLinksWithParent(beanName).map { it.from.name }
    }

    override fun getLinksWithParent(beanName: String): List<BeanDependencyLink> {
        if(!reverseLinkMap.containsKey(beanName))
            return listOf()

        return reverseLinkMap[beanName]!!
    }

    override fun isRoot(beanName: String): Boolean {
        return rootNodeNameSet.contains(beanName)
    }

    override fun getRootLinks(): List<BeanDependencyLink> {
        return rootNodeNameSet
            .filter(linkMap::containsKey)
            .map { linkMap.getOrDefault(it, ArrayList()) }
            .flatten()
    }

    override fun getRootNames(): List<String> {
        return rootNodeNameSet.map { it }
    }



    override fun getLinks(): List<BeanDependencyLink> {
        return linkList
    }

    override fun getLinks(name: String): List<BeanDependencyLink> {
        if(!linkMap.containsKey(name))
            return listOf()

        return linkMap[name]!!
    }



    override fun getLinkedBeanNames(): Set<String> {
        return linkedBeanNameList;
    }

}
