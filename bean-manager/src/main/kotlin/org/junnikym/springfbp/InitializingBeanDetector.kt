package org.junnikym.springfbp

import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.annotation.Configuration

@Configuration
@IgnoreManage
class InitializingBeanDetector(
        private val beanFactory: ConfigurableListableBeanFactory,
        private val linkFactory: BeanDependencyLinkFactory,
        private val beanManagingTargetFilter: BeanManagingTargetFilter,
) : InitializingBean {

    override fun afterPropertiesSet() {
        beanFactory.beanDefinitionNames
            .mapNotNull(::getLink)
            .forEach(linkFactory::add);
    }

    private fun getLink(beanName: String): List<BeanDependencyLink>? {
        val self = getBeanDependencyNode(beanName) ?: return null;
        return getDependencies(beanName).map { BeanDependencyLink(self, it) };
    }

    private fun getDependencies(beanName: String): List<BeanDependencyNode> {
        return beanFactory
            .getDependentBeans(beanName)
            .mapNotNull(::getBeanDependencyNode);
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