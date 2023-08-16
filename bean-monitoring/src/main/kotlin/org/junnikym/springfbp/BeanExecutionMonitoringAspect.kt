package org.junnikym.springfbp

import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import java.lang.reflect.Method

class BeanExecutionMonitoringAspect(
        private val bean: Any,
        private val beanManagingTargetFilter: BeanManagingTargetFilter,
        private val beanExecutionMonitoringService: BeanExecutionMonitoringService,
) : MethodInterceptor {

    override fun invoke(invocation: MethodInvocation): Any? {
        val event = BeanEvent(
                bean = bean,
                from = getTargetMethodFrom(invocation.method),
                method = invocation.method,
        )
        beanExecutionMonitoringService.execute(event)

        val result = invocation.proceed()

        beanExecutionMonitoringService.exit(event)
        return result;
    }

    private fun getTargetMethodFrom(method: Method): StackTraceElement? {
        val stackTrace = Thread.currentThread().stackTrace
                .filter { beanManagingTargetFilter.isInBasePackage(it.className) }
                .filter { beanManagingTargetFilter.isIgnoreManage(Class.forName(it.className)).not() }

        val currentMethodIndex = stackTrace.indexOfFirst {
            val eqClass = it.className.startsWith(method.declaringClass.name)
            val eqName = it.methodName == method.name
            eqClass && eqName
        }

        if(currentMethodIndex < 0 || stackTrace.size-1 <= currentMethodIndex)
            return null;

        return stackTrace[currentMethodIndex+1]
    }

}