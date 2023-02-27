package org.junnikym.springfbp.service

import org.junnikym.springfbp.BeanDependencyLink
import org.junnikym.springfbp.BeanDependencyLinkFactory
import org.springframework.stereotype.Component

@Component
class BeanLayerFactoryImpl(
    private val beanDependencyLinkFactory: BeanDependencyLinkFactory
): BeanLayerFactory {

    private var layerMap: Map<String, Int> = mapOf();

    override fun update() {
        val newLayerMap: HashMap<String, Int> = HashMap();
        val histories : HashSet<String> = HashSet();

        beanDependencyLinkFactory.getRootNames()
            .forEach { searchLinkBranch(0, it, newLayerMap, histories) }

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
        getLinks(beanName)
            .forEach { searchLinkBranch(layer+1, it.to.name, newLayerMap, histories) }

        histories.remove(beanName)
    }

    /**
     * get links which linked with target
     *
     *     it returns list of links which linked with target.
     *     when exception occurs, it returns empty list.
     *
     * @param beanName target bean name
     * @return returning links
     */
    private fun getLinks(beanName: String): List<BeanDependencyLink> {
        return try {
            beanDependencyLinkFactory.getLinks(beanName)
        } catch (e: Exception) {
            listOf()
        }
    }

}