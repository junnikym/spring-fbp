package org.junnikym.springfbp

import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.annotation.Configuration

@Configuration
class InitializingBeanDetector : InitializingBean {

    private val beanFactory: ConfigurableListableBeanFactory;

    private val linkFactory: BeanDependencyLinkFactory;

    constructor(
        beanFactory: ConfigurableListableBeanFactory,
        linkFactory: BeanDependencyLinkFactory
    ) {
        this.beanFactory = beanFactory;
        this.linkFactory = linkFactory;
    }

    override fun afterPropertiesSet() {
        beanFactory.beanDefinitionNames
            .mapNotNull(::getLink)
            .forEach(linkFactory::add);

        println(linkFactory);
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

        val bean = beanFactory.getBean(beanName);
        if(bean.javaClass.`package`.equals(this.javaClass.`package`))
            return null;

        val definition = beanFactory.getBeanDefinition(beanName);

        return BeanDependencyNode(beanName, bean, definition);
    }

}