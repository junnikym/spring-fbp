package org.junnikym.springfbp.service

import org.junnikym.springfbp.BeanDependencyLinkFactory
import org.junnikym.springfbp.BeanWithLayer
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class LayerQueryServiceImpl (
    private val beanDependencyLinkFactory: BeanDependencyLinkFactory,
    private val beanLayerFactory: BeanLayerFactory
): LayerQueryService {

    override fun get(): List<List<BeanWithLayer>> {
        beanLayerFactory.update()

        val layerMap = beanDependencyLinkFactory
            .getLinkedBeans().parallelStream()
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
        return BeanWithLayer(
            beanName,
            beanLayerFactory.get(beanName)
        );
    }

}
