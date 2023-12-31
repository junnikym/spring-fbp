package org.junnikym.springfbp

import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.junnikym.springfbp.filter.IgnoreManage

@IgnoreManage
class BeanExecutionMonitoringAspect(
        private val bean: Any,
        private val beanExecutionMonitoringService: BeanExecutionMonitoringService,
) : MethodInterceptor {

    override fun invoke(invocation: MethodInvocation): Any? {
        beanExecutionMonitoringService.execute(bean, invocation.method)
        val result = invocation.proceed()
        beanExecutionMonitoringService.exit(invocation.method)

        return result
    }

}