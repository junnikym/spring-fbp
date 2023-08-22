package org.junnikym.springfbp.beans

import org.junnikym.springfbp.common.BeanDependencyLink
import org.junnikym.springfbp.common.BeanDependencyNode
import org.junnikym.springfbp.factory.BeanDependencyLinkFactory
import org.junnikym.springfbp.factory.BeanDependencyNodeFactory
import org.junnikym.springfbp.factory.DefaultBeanDependencyLinkFactory
import org.junnikym.springfbp.service.BeanLayerFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.stereotype.Component

@Component
class TestFactoryProvider(
        private val beanFactory: ConfigurableListableBeanFactory,
        private val beanLayerFactory: BeanLayerFactory,
        private val beanDependencyNodeFactory: BeanDependencyNodeFactory,
        private val beanDependencyLinkFactory: DefaultBeanDependencyLinkFactory,
) {

    private var layerFactory: BeanLayerFactory? = null
    private var nodeFactory: BeanDependencyNodeFactory? = null
    private var linkFactory: BeanDependencyLinkFactory? = null

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

    fun layerFactoryOf(): BeanLayerFactory {
        if(this.layerFactory != null)
            return this.layerFactory!!

        val nodeFactory = nodeFactoryOf()
        val linkFactory = linkFactoryOf()
        val layerFactory = beanLayerFactory
                .javaClass
                .constructors[0]
                .newInstance(linkFactory, nodeFactory) as BeanLayerFactory

        this.layerFactory = layerFactory

        return layerFactory;
    }

    fun nodeFactoryOf(): BeanDependencyNodeFactory {
        if(nodeFactory != null)
            return nodeFactory!!

        val factory = beanDependencyNodeFactory::class.constructors.first().call()

        listOf(
                beanDependencyNodeOf("testLeafBean"),
                beanDependencyNodeOf("testInternalBeanA"),
                beanDependencyNodeOf("testInternalBeanB"),
                beanDependencyNodeOf("testInternalBeanC"),
                beanDependencyNodeOf("testRootBean"),
        ).forEach(factory::add)

        nodeFactory = factory

        return factory
    }

    fun linkFactoryOf(): BeanDependencyLinkFactory {
        if(linkFactory != null)
            return linkFactory!!

        val factory = beanDependencyLinkFactory::class.constructors.first().call()

        val rootNode = beanDependencyNodeOf("testRootBean")

        val internalNodeB = beanDependencyNodeOf("testInternalBeanA")
        val internalNodeA = beanDependencyNodeOf("testInternalBeanB")
        val internalNodeC = beanDependencyNodeOf("testInternalBeanC")

        val leafNode = beanDependencyNodeOf("testLeafBean")

        listOf(
                BeanDependencyLink(internalNodeB, rootNode),
                BeanDependencyLink(internalNodeA, rootNode),
                BeanDependencyLink(internalNodeC, rootNode),
                BeanDependencyLink(leafNode, internalNodeA)
        ).forEach(factory::add)

        linkFactory = factory

        return factory
    }

    private fun beanDependencyNodeOf(beanName: String): BeanDependencyNode {
        val bean = beanFactory.getBean(beanName)
        val definition = beanFactory.getBeanDefinition(beanName)
        return BeanDependencyNode(beanName, bean, definition)
    }


}