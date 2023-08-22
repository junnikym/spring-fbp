package org.junnikym.springfbp

import org.junnikym.springfbp.common.BeanDependencyLink
import org.junnikym.springfbp.common.BeanDependencyNode
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
        private val beanManagingTargetFilter: BeanManagingTargetFilter,
) : InitializingBean {

    override fun afterPropertiesSet() {
        addNodesInFactory()
        addLinksInFactory()
    }

    private fun addNodesInFactory() {
        beanFactory.beanDefinitionNames
                .mapNotNull(::getBeanDependencyNode)
                .forEach(nodeFactory::add)
    }

    private fun addLinksInFactory() {
        nodeFactory.getAll()
                .mapNotNull(::getLinks)
                .forEach(linkFactory::add);
    }

    private fun getLinks(node: BeanDependencyNode): List<BeanDependencyLink>? {
        return beanFactory.getDependentBeans(node.name)
                .mapNotNull(nodeFactory::get)
                .map { other-> BeanDependencyLink(node, other) };
    }

    private fun getBeanDependencyNode(beanName: String): BeanDependencyNode? {
        if(beanName == "initializingBeanDetector")
            return null;

        val bean = beanFactory.getBean(beanName)
        val beanClass = AopProxyUtils.ultimateTargetClass(bean);
        if(!beanManagingTargetFilter.isManageTarget(beanClass))
            return null;

        val definition = beanFactory.getBeanDefinition(beanName);
        return BeanDependencyNode(beanName, bean, definition);
    }

}