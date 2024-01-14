package org.junnikym.springfbp

import org.junnikym.springfbp.filter.IgnoreManage
import org.springframework.aop.framework.ProxyFactory
import org.springframework.stereotype.Component

@Component
@IgnoreManage
class BeanMonitoringProxyFactory(
        private val beanExecutionMonitoringService: BeanExecutionMonitoringService,
) {

    /**
     * create proxy object for monitoring bean
     *
     * @param bean target bean for proxy
     * @return proxied target bean
     */
    fun of (bean: Any): Any {
        val proxyFactory = ProxyFactory(bean)
        val aspect = BeanExecutionMonitoringAspect(bean, beanExecutionMonitoringService)
        proxyFactory.addAdvice(aspect)
        return proxyFactory.proxy
    }

}