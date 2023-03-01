package org.junnikym.springfbp.beans

import org.junnikym.springfbp.BeanDependencyLink
import org.junnikym.springfbp.BeanDependencyLinkFactory
import org.junnikym.springfbp.BeanDependencyNode
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.util.Arrays

/**
 * [ Test Bean Structure ]
 *            ┌─────────┐   ┌──────┐
 *          ┌─┤ middleA ├───┤ last │
 *          │ └─────────┘   └──────┘
 * ┌──────┐ │ ┌─────────┐
 * │ root ├─┼─┤ middleB │
 * └──────┘ │ └─────────┘
 *          │ ┌─────────┐
 *          └─┤ middleC │
 *            └─────────┘
 */
class TestBeanDependencyLinkFactory(
    private val beanFactory: ConfigurableListableBeanFactory
): BeanDependencyLinkFactory {

    private val linkMap : HashMap<String, List<BeanDependencyLink>> = HashMap()

    private val links : ArrayList<BeanDependencyLink> = ArrayList()

    private val linkedBeanList : HashSet<String> = HashSet()

    private val rootNodeNames : ArrayList<String> = ArrayList()

    private val rootLinks : ArrayList<BeanDependencyLink> = ArrayList()

    init {
        initLinks()
    }

    private fun initLinks() {

        val leafNode = getBeanDependencyNode("testLeafBean")

        val internalNodeA = getBeanDependencyNode("testInternalBeanA")
        val internalNodeB = getBeanDependencyNode("testInternalBeanB")
        val internalNodeC = getBeanDependencyNode("testInternalBeanC")

        rootNodeNames.add("testRootBean")
        val rootNode = getBeanDependencyNode("testRootBean")

        val rootLink = listOf(
            BeanDependencyLink(rootNode, internalNodeA),
            BeanDependencyLink(rootNode, internalNodeB),
            BeanDependencyLink(rootNode, internalNodeC),
        )
        linkMap["testRootBean"] = rootLink
        links.addAll(rootLink)
        rootLinks.addAll(rootLink)

        val internalALink = listOf(
            BeanDependencyLink(internalNodeA, leafNode)
        )
        linkMap["testInternalBeanA"] = internalALink
        links.addAll(internalALink)
    }

    private fun getBeanDependencyNode(beanName: String): BeanDependencyNode {
        linkedBeanList.add(beanName)
        val bean = beanFactory.getBean(beanName)
        val definition = beanFactory.getBeanDefinition(beanName)
        return BeanDependencyNode(beanName, bean, definition)
    }

    override fun add(link: BeanDependencyLink) {
        TODO("Not use")
    }

    override fun add(link: List<BeanDependencyLink>) {
        TODO("Not use")
    }

    override fun hasParent(nodeName: String): Boolean {
        TODO("Not use")
    }

    override fun hasParentOfFromNode(link: BeanDependencyLink): Boolean {
        TODO("Not use")
    }

    override fun getParentNames(beanName: String): List<String> {
        TODO("Not use")
    }

    override fun getLinksWithParent(beanName: String): List<BeanDependencyLink> {
        TODO("Not use")
    }

    override fun isRoot(beanName: String): Boolean {
        TODO("Not use")
    }

    override fun getRootLinks(): List<BeanDependencyLink> {
        return rootLinks
    }

    override fun getRootNames(): List<String> {
        return rootNodeNames
    }

    override fun getLinks(): List<BeanDependencyLink> {
        return links
    }

    override fun getLinks(name: String): List<BeanDependencyLink> {
        if(!linkMap.containsKey(name))
            return listOf()

        return linkMap[name]!!
    }

    override fun getLinkedBeanNames(): Set<String> {
        return linkedBeanList;
    }

}
