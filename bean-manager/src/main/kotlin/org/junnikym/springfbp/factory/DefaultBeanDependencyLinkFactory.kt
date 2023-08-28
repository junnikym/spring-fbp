package org.junnikym.springfbp.factory

import org.junnikym.springfbp.common.BeanDependencyLink
import org.springframework.stereotype.Component

@Component
class DefaultBeanDependencyLinkFactory : BeanDependencyLinkFactory {

    // key : name of from (bean); parent node
    // val : child node
    private val linkNameMap =
            mutableMapOf<String, MutableList<BeanDependencyLink>>()
                    as MutableMap<String, MutableCollection<BeanDependencyLink>>

    // key : name of to (bean); child node
    // val : parent node
    private val reverseLinkMap =
            mutableMapOf<String, MutableList<BeanDependencyLink>>()
                    as MutableMap<String, MutableCollection<BeanDependencyLink>>

    private val linkList = mutableListOf<BeanDependencyLink>()

    private val rootNodeNameSet = mutableSetOf<String>()

    private val linkedBeanNameList = mutableSetOf<String>()

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
        addToMap(name, link, linkNameMap)

        val clazz = AopProxyUtils.ultimateTargetClass(link.to.bean);
        addToMap(clazz, link, linkClassMap)
    }

    private fun addToReverseLinkMap(link: BeanDependencyLink) {
        val name = link.from.name
        addToMap(name, link, reverseLinkMap)
    }

    private fun <K> addToMap(
            key: K,
            link: BeanDependencyLink,
            map: MutableMap<K, MutableCollection<BeanDependencyLink>>
    ) {
        val linkList = map.getOrDefault(key, ArrayList())
        linkList.add(link)
        map[key] = linkList
    }

    private fun updateRootNodeNames(link: BeanDependencyLink) {
        // [ remove 'from' in rootSet ]
        if(rootNodeNameSet.contains(link.from.name))
            rootNodeNameSet.remove(link.from.name)

        // [ Add to rootSet ]
        // If 'to' has parent when 'to' is 'from', 'to' can't to be root
        val isVerifiedNotRoot = reverseLinkMap[link.to.name]?.size?.let { it > 0 } ?: false
        if(isVerifiedNotRoot)
            return

        // Add 'to' on rootSet
        rootNodeNameSet.add(link.to.name)
    }



    override fun hasParent(nodeName: String): Boolean {
        return reverseLinkMap.containsKey(nodeName)
    }

    override fun hasParentOfFromNode(link: BeanDependencyLink): Boolean {
        return reverseLinkMap.containsKey(link.to.name)
    }

    override fun getParentNames(beanName: String): Collection<String> {
        return getLinksWithParent(beanName).map { it.to.name }
    }

    override fun getLinksWithParent(beanName: String): Collection<BeanDependencyLink> {
        if(!reverseLinkMap.containsKey(beanName))
            return listOf()

        return reverseLinkMap[beanName]!!
    }

    override fun isRoot(beanName: String): Boolean {
        return rootNodeNameSet.contains(beanName)
    }

    override fun getRootLinks(): Collection<BeanDependencyLink> {
        return rootNodeNameSet
            .filter(linkNameMap::containsKey)
            .map { linkNameMap.getOrDefault(it, ArrayList()) }
            .flatten()
    }

    override fun getRootNames(): Collection<String> {
        return rootNodeNameSet.map { it }
    }



    override fun getLinks(): Collection<BeanDependencyLink> {
        return linkList
    }

    override fun getLinks(name: String): Collection<BeanDependencyLink> {
        if(!linkNameMap.containsKey(name))
            return listOf()

        return linkNameMap[name]!!
    }

    override fun getLinkedBeanNames(): Set<String> {
        return linkedBeanNameList;
    }

}
