package org.junnikym.springfbp

import org.junnikym.springfbp.common.BeanDependencyLink
import org.junnikym.springfbp.common.BeanDependencyNode
import org.junnikym.springfbp.common.DetectedUnmanagedClass
import org.junnikym.springfbp.factory.BeanDependencyLinkFactory
import org.junnikym.springfbp.factory.BeanDependencyNodeFactory
import org.junnikym.springfbp.filter.BeanManagingTargetFilter
import org.junnikym.springfbp.filter.IgnoreManage
import org.springframework.aop.framework.AopProxyUtils
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.annotation.Configuration

@Configuration
@IgnoreManage
class InitializingBeanDetector(
        private val beanFactory: ConfigurableListableBeanFactory,
        private val linkFactory: BeanDependencyLinkFactory,
        private val nodeFactory: BeanDependencyNodeFactory,
        private val unmanagedClassDetector: UnmanagedClassDetector,
        private val beanManagingTargetFilter: BeanManagingTargetFilter,
) : InitializingBean {

    override fun afterPropertiesSet() {
        addManagedClasses()
        addUnmanagedClasses()
    }

    private fun addManagedClasses() {
        beanFactory.beanDefinitionNames
                .mapNotNull(::getBeanDependencyNode)
                .forEach { node->
                    val link = beanFactory
                            .getDependentBeans(node.name)
                            .mapNotNull(nodeFactory::get)
                            .map { other-> BeanDependencyLink(node, other) }

                    nodeFactory.add(node)
                    linkFactory.add(link)
                }
    }

    private fun addUnmanagedClasses() {
        val detectedUnmanagedClasses = unmanagedClassDetector.detectInFactory()

        // add into node factory
        addUnmanagedClassesNodeIntoFactory(detectedUnmanagedClasses, DetectedUnmanagedClass::fromClass)
        addUnmanagedClassesNodeIntoFactory(detectedUnmanagedClasses, DetectedUnmanagedClass::generatedClass)

        val test = detectedUnmanagedClasses.mapNotNull {
            val fromNode = nodeFactory.get(it.fromClass)
            val generatedNode = nodeFactory.get(it.generatedClass)

            if(fromNode == null || generatedNode == null)
                null
            else
                BeanDependencyLink(generatedNode, fromNode)
        }.distinct()

        test.forEach(linkFactory::add)
    }

    private fun getBeanDependencyNode(beanName: String): BeanDependencyNode? {
        if(beanName == "initializingBeanDetector")
            return null;

        val bean = beanFactory.getBean(beanName)
        val beanClass = AopProxyUtils.ultimateTargetClass(bean);
        if(!beanManagingTargetFilter.isManageTarget(beanClass))
            return null;

        return BeanDependencyNode(beanName, bean);
    }

    private fun addUnmanagedClassesNodeIntoFactory(
            classes: Collection<DetectedUnmanagedClass>,
            classGetter: (DetectedUnmanagedClass)-> Class<*>
    ) {
        return classes.distinctBy(classGetter)
                .filter { nodeFactory.exists(classGetter(it)).not() }
                .map {
                    val cls = classGetter(it)
                    BeanDependencyNode(cls.name, cls)
                }
                .forEach(nodeFactory::add)
    }

}