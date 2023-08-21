package org.junnikym.springfbp.service

import org.junnikym.springfbp.BeanDependencyLink
import org.junnikym.springfbp.BeanDependencyLinkFactory
import org.junnikym.springfbp.BeanDependencyNode
import org.junnikym.springfbp.BeanDependencyNodeFactory
import org.springframework.stereotype.Component

@Component
class BeanLayerFactoryImpl(
        private val beanDependencyLinkFactory: BeanDependencyLinkFactory,
        private val beanDependencyNodeFactory: BeanDependencyNodeFactory,
): BeanLayerFactory {

    private var layerMap: Map<String, Int> = mapOf();

    override fun update() {
        val newLayerMap: HashMap<String, Int> = HashMap();
        val histories : HashSet<String> = HashSet();

        beanDependencyNodeFactory.getAllNames().forEach { searchLinkBranch(0, it, newLayerMap, histories) }
        this.layerMap = newLayerMap;
    }

    override fun get(beanName: String): Int {
        return layerMap[beanName]!!;
    }

    /**
     * Search for layers of bean by DFS
     *
     * @param layer layer number of the target to search for
     * @param beanName bean name of the target to search for
     * @param newLayerMap [HashMap] object which for recording layers of beans
     * @param histories [HashSet] object which for recording searched target histories
     */
    private fun searchLinkBranch(
        layer: Int,
        beanName: String,
        newLayerMap: HashMap<String, Int>,
        histories: HashSet<String>
    ) {
        if(histories.contains(beanName))  // circular linked
            return;

        histories.add(beanName)

        // put the greater value
        val recordedLayer = newLayerMap.getOrDefault(beanName, 0);
        newLayerMap[beanName] = layer.coerceAtLeast(recordedLayer);

        // next step
        beanDependencyLinkFactory.getLinks(beanName)
            .forEach { searchLinkBranch(layer+1, it.from.name, newLayerMap, histories) }

        histories.remove(beanName)
    }

}