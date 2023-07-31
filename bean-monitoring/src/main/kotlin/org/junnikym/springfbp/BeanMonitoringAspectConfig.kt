package org.junnikym.springfbp

import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Configuration

@Configuration
class BeanMonitoringAspectConfig(
        @Value("\${spring.fbp.base-package}") private val basePackage: String,
        private val beanExecutionMonitoringAspect: BeanExecutionMonitoringAspect,
) : BeanPostProcessor {

    init {

    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        val proxyBean = if(isToBeMonitoring(bean)) createProxy(bean) else bean
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
        proxyFactory.addAdvice(beanExecutionMonitoringAspect)
        return proxyFactory.proxy
    }

    /**
     * check that is bean to be monitoring
     *
     * @param bean target for check
     * @return true and false, Whether it is monitored
     */
    private fun isToBeMonitoring(bean: Any): Boolean {
        val beanClass = bean.javaClass;
        val isInPackage = beanClass.`package`.name.startsWith(basePackage)
        val isIgnoreMonitoring = beanClass.annotations.any {
            when(it.annotationClass) {
                SpringBootApplication::class, IgnoreMonitoring::class -> true
                else -> false
            }
        }

        return isInPackage && !isIgnoreMonitoring
    }

}