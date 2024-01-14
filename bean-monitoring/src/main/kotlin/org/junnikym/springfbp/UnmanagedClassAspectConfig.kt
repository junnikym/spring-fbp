package org.junnikym.springfbp

import org.junnikym.springfbp.common.BeanDependencyNode
import org.junnikym.springfbp.factory.BeanDependencyNodeFactory
import org.junnikym.springfbp.filter.IgnoreManage
import org.springframework.aop.framework.AopProxyUtils
import org.springframework.aop.target.SingletonTargetSource
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.annotation.Configuration
import java.lang.reflect.Field

@Configuration
@IgnoreManage
class UnmanagedClassAspectConfig (
        private val beanFactory: ConfigurableListableBeanFactory,
        private val nodeFactory: BeanDependencyNodeFactory,
        private val proxyFactory: BeanMonitoringProxyFactory,
) : SynchronizedInitializingBean(InitializingBeanDetector::class.java) {

    override fun afterPropertiesSet() {
        nodeFactory.getAll()
                .filter(BeanDependencyNode::isManaged)
                .forEach { applyProxy(it) }
    }

    private fun applyProxy(node: BeanDependencyNode) {
        val originalBean = getTargetOfProxy(node.bean!!)
        val targets = getFieldsNeedToBeProxy(node)
        targets.forEach{ proxyField(it, originalBean) }
    }

    private fun getTargetOfProxy(proxy: Any): Any {
        return proxy.javaClass
                .getDeclaredMethod("getTargetSource").invoke(proxy)
                .let { it as SingletonTargetSource }
                .let { it.target!! }
    }

    private fun getFieldsNeedToBeProxy(node: BeanDependencyNode): List<Field> {
        if(node.beanName.isNullOrBlank())
            return listOf()

        val dependencies = beanFactory
                .getDependenciesForBean(node.beanName!!)
                .map(beanFactory::getBean)
                .map(AopProxyUtils::ultimateTargetClass)
                .toSet()

        return node.clazz.declaredFields
                .filter { dependencies.contains(it.type).not() }
    }

    private fun proxyField (field: Field, fieldOwner: Any) {
        field.isAccessible = true
        val fieldValue = field.get(fieldOwner)
        field.set(fieldOwner, proxyFactory.of(fieldValue))
    }

}