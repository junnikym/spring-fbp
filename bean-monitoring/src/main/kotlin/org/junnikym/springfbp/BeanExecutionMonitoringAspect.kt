package org.junnikym.springfbp

import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.stereotype.Component

@Component
class BeanExecutionMonitoringAspect : MethodInterceptor {

    override fun invoke(invocation: MethodInvocation): Any? {
        println(" - method start - ");
        val result = invocation.proceed();
        println(result)
        println(" - method end - ");
        return result;
    }

}