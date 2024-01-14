package org.junnikym.springfbp

import org.junnikym.springfbp.common.BeanDependencyLink
import org.junnikym.springfbp.common.BeanDependencyNode
import org.junnikym.springfbp.common.DetectedUnmanagedClass
import org.junnikym.springfbp.factory.BeanDependencyLinkFactory
import org.junnikym.springfbp.factory.BeanDependencyNodeFactory
import org.junnikym.springfbp.filter.BeanManagingTargetFilter
import org.junnikym.springfbp.filter.IgnoreManage
import org.springframework.aop.framework.AopProxyUtils
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.annotation.Configuration
import kotlin.reflect.full.functions

@Configuration
@IgnoreManage
class InitializingBeanDetector(
        private val beanFactory: ConfigurableListableBeanFactory,
        private val linkFactory: BeanDependencyLinkFactory,
        private val nodeFactory: BeanDependencyNodeFactory,
        private val unmanagedClassDetector: UnmanagedClassDetector,
        private val beanManagingTargetFilter: BeanManagingTargetFilter,
) : SynchronizedInitializingBean() {

    override fun afterPropertiesSet() {
        addManagedClasses()
        addUnmanagedClasses()
    }

    private fun addManagedClasses() {
        val nodes = beanFactory.beanDefinitionNames.flatMap(::beanDependencyNodeOf)

        nodes.forEach(nodeFactory::add)
        nodes.forEach { node->
            val link = beanFactory
                .getDependenciesForBean(node.beanName!!)
                .mapNotNull(nodeFactory::get)
                .map { other-> BeanDependencyLink(node, other) }

            linkFactory.add(link)
        }
    }

    private fun addUnmanagedClasses() {
        val detectedUnmanagedClasses = unmanagedClassDetector.detectInFactory()

        // add into node factory
        addUnmanagedClassesNodeIntoFactory(detectedUnmanagedClasses, DetectedUnmanagedClass::fromClass)
        addUnmanagedClassesNodeIntoFactory(detectedUnmanagedClasses, DetectedUnmanagedClass::generatedClass)

        detectedUnmanagedClasses.mapNotNull {
            val fromNode = nodeFactory.get(it.fromClass)
            val generatedNode = nodeFactory.get(it.generatedClass)

            if(fromNode == null || generatedNode == null)
                null
            else
                BeanDependencyLink(fromNode, generatedNode)
        }.distinct().forEach(linkFactory::add)
    }

    private fun beanDependencyNodeOf(beanName: String): List<BeanDependencyNode> {
        if(beanName == "initializingBeanDetector" || beanName == "synchronizedInitializingBeanExecutor")
            return listOf();

        val bean = beanFactory.getBean(beanName)
        val beanClass = AopProxyUtils.ultimateTargetClass(bean);
        if(!beanManagingTargetFilter.isManageTarget(beanClass))
            return listOf();

        val classNode = BeanDependencyNode(
            nodeName = beanClass.name, beanName = beanName,
            clazz = beanClass, bean = bean
        )

        val interfaces = beanClass.interfaces
        if(interfaces.isEmpty())
            return classNode.let(::listOf)

        val result = interfaces.map {
            BeanDependencyNode(
                nodeName = it.name, beanName = beanName,
                clazz = it, bean = bean
            )
        }.toMutableList()
        if(existNonInterfaceFunction(beanClass))
            result.add(classNode)

        return result
    }

    private fun existNonInterfaceFunction(clazz: Class<*>): Boolean {
        val functions = clazz.declaredMethods.toMutableSet()
        clazz.interfaces
            .flatMap { it::class.java.declaredMethods.toList() }
            .forEach { functions.remove(it) }

        return functions.isNotEmpty()
    }

    private fun addUnmanagedClassesNodeIntoFactory(
            classes: Collection<DetectedUnmanagedClass>,
            classGetter: (DetectedUnmanagedClass)-> Class<*>
    ) {
        return classes.distinctBy(classGetter)
                .filter { nodeFactory.exists(classGetter(it)).not() }
                .map {
                    val cls = classGetter(it)
                    BeanDependencyNode(
                        nodeName = cls.name, beanName = null,
                        clazz = cls, bean = null
                    )
                }
                .forEach(nodeFactory::add)
    }

}