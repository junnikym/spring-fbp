package org.junnikym.springfbp

import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.annotation.Configuration

@Configuration
class IntializeingBeanDetector : InitializingBean {

    val beanFactory: ConfigurableListableBeanFactory;

    constructor(beanFactory: ConfigurableListableBeanFactory) {
        this.beanFactory = beanFactory
    }

    override fun afterPropertiesSet() {
        beanFactory.beanDefinitionNames.map(::getLink);
    }

    private fun getLink(beanName: String): List<BeanDependencyLink> {
        val self = getBean(beanName);

        return getDependencies(beanName).map { BeanDependencyLink(self, it) };
    }

    private fun getDependencies(beanName: String): List<Any?> {
        return beanFactory
            .getDependentBeans(beanName)
            .map(::getBean);
    }

    private fun getBean(beanName: String): Any? {
        if(beanName.equals("intializeingBeanDetector"))
            return null;

        return beanFactory.getBean(beanName);
    }

}