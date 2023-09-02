package org.junnikym.springfbp

import org.junnikym.springfbp.filter.BeanManagingTargetFilter
import org.junnikym.springfbp.filter.IgnoreManage
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Configuration

@Configuration
@IgnoreManage
class BeanMonitoringAspectConfig(
        private val beanManagingTargetFilter: BeanManagingTargetFilter,
        private val beanMonitoringProxyFactory: BeanMonitoringProxyFactory,
) : BeanPostProcessor {

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        val isTarget = beanManagingTargetFilter.isManageTarget(bean::class)
        val proxyBean = if(isTarget) beanMonitoringProxyFactory.of(bean) else bean

        return super.postProcessAfterInitialization(proxyBean, beanName)
    }

}