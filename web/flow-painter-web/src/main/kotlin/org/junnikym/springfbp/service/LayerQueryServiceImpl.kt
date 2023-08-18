package org.junnikym.springfbp.service

import org.junnikym.springfbp.BeanDependencyLinkFactory
import org.junnikym.springfbp.BeanWithLayer
import org.springframework.aop.framework.AopProxyUtils
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class LayerQueryServiceImpl (
        private val beanFactory: ConfigurableListableBeanFactory,
        private val beanDependencyLinkFactory: BeanDependencyLinkFactory,
        private val beanLayerFactory: BeanLayerFactory
): LayerQueryService {

    override fun get(): List<List<BeanWithLayer>> {
        beanLayerFactory.update()

        val layerMap = beanDependencyLinkFactory
            .getLinkedBeanNames().parallelStream()
            .map(::beanWithLayerOf)
            .collect(Collectors.groupingBy { it.layer })

        val result: ArrayList<List<BeanWithLayer>> = ArrayList()
        val lastLayer: Int = layerMap.keys.maxOrNull()?:0
        (0..lastLayer).forEach run@ {
            if(!layerMap.containsKey(it))
                return@run

            result.add(layerMap[it]!!)
        }

        return result
    }

    override fun getLayer(beanName: String): Int {
        return beanLayerFactory.get(beanName);
    }

    private fun beanWithLayerOf(beanName: String): BeanWithLayer {
        val linkedWith = beanDependencyLinkFactory
                .getLinks(beanName)
                .map {
                    val cls = AopProxyUtils.ultimateTargetClass(it.to.bean)
                    BeanWithLayer.LinkedBean (
                            beanName = it.to.name,
                            beanClassQualifiedName = cls.name,
                            beanClassSimpleName = cls.simpleName
                    )
                }

        val beanClass = AopProxyUtils.ultimateTargetClass(beanFactory.getBean(beanName))

        return BeanWithLayer(
                beanName = beanName,
                beanClassQualifiedName = beanClass.name,
                beanClassSimpleName = beanClass.simpleName,
                linkedWith = linkedWith,
                layer = beanLayerFactory.get(beanName),
        )
    }

}
