package org.junnikym.springfbp

import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Configuration

@Configuration
class BeanMonitoringAspectConfig(
        private val beanManagingTargetFilter: BeanManagingTargetFilter,
) : BeanPostProcessor {

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        val isTarget = beanManagingTargetFilter.isManageTarget(bean::class)
        val proxyBean = if(isTarget) createProxy(bean) else bean

        return super.postProcessAfterInitialization(proxyBean, beanName)
    }

    /**
     * create proxy object for monitoring bean
     *
     * @param bean target bean for proxy
     * @return proxied target bean
     */
    private fun createProxy(bean: Any): Any {
        println(bean.javaClass.name)
        val proxyFactory = ProxyFactory(bean)
        val aspect = BeanExecutionMonitoringAspect(beanManagingTargetFilter, bean)
        proxyFactory.addAdvice(aspect)
        return proxyFactory.proxy
    }

}