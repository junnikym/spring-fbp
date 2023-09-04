package org.junnikym.springfbp

import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration
import org.springframework.util.ClassUtils

abstract class SynchronizedInitializingBean(vararg dependOn: Class<out SynchronizedInitializingBean>) {

    init { reservation(this, *dependOn) }

    abstract fun afterPropertiesSet()

    companion object {

        class DuplicateExecutionException(initializerClass: Class<out SynchronizedInitializingBean>)
            : Exception("InitializingBean duplicate execution occurred : $initializerClass")

        private val executionHistory = mutableSetOf<Class<*>>()

        private val retryMap =
                mutableMapOf<SynchronizedInitializingBean, Array<out Class<out SynchronizedInitializingBean>>>()

        fun reservation(
                initializer: SynchronizedInitializingBean,
                vararg dependOn: Class<out SynchronizedInitializingBean>
        ) {
            if (retryMap.keys.contains(initializer))
                throw DuplicateExecutionException(initializer.javaClass)

            retryMap[initializer] = dependOn
        }

        fun executeAll() {
            retryMap.keys.forEach { execute(it, *retryMap[it]!!) }
        }

        private fun execute(
                initializer: SynchronizedInitializingBean,
                vararg dependOn: Class<out SynchronizedInitializingBean>
        ) {
            if (!canExecution(initializer, *dependOn))
                return

            executionHistory.add(ClassUtils.getUserClass(initializer))
            retryMap.remove(initializer)
            initializer.afterPropertiesSet()

            executeAll()
        }

        private fun canExecution(
                initializer: SynchronizedInitializingBean,
                vararg dependOn: Class<out SynchronizedInitializingBean>
        ): Boolean {
            if (executionHistory.contains(initializer.javaClass))
                return false

            if (dependOn.isEmpty())
                return true

            for(it in dependOn) {
                if(executionHistory.contains(it).not())
                    return false
            }

            return true
        }

    }

}

@Configuration
class SynchronizedInitializingBeanExecutor: InitializingBean {

    override fun afterPropertiesSet() {
        SynchronizedInitializingBean.executeAll()
    }

}