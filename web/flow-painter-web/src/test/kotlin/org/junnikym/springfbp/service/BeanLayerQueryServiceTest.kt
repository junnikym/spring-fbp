package org.junnikym.springfbp.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(locations= ["classpath:test.properties"])
class BeanLayerQueryServiceTest(
        @Autowired private val layerQueryService: LayerQueryService,
) {

    @Test
    fun get_all_beans_with_layer_which_organized_by_layer() {
        // given

        // when
        val beansWithLayer = layerQueryService.get()

        // then
        assertEquals(beansWithLayer.size, 3, "layer list size of result")

        assertEquals(beansWithLayer[0].size, 1, "bean list size of root bean layer")
        assertEquals(beansWithLayer[1].size, 3, "bean list size of internal bean layer")
        assertEquals(beansWithLayer[2].size, 1, "bean list size of leaf bean layer")

        assertEquals(
            beansWithLayer[0][0].beanName, "testRootBean",
            "is correct bean name of root bean at correct position"
        )

        val internalBeanSet = HashSet(beansWithLayer[1].map { it.beanName })
        beansWithLayer[1].forEach {
            assertTrue(
                internalBeanSet.contains(it.beanName),
                "is correct bean name of internal bean at correct position"
            )
        }

        assertEquals(
            beansWithLayer[2][0].beanName, "testLeafBean",
            "is correct bean name of leaf bean at correct position"
        )
    }

    @Test
    fun get_layer_of_bean () {
        // given

        // when
        val root = layerQueryService.getLayer("testRootBean")
        val internalA = layerQueryService.getLayer("testInternalBeanA")
        val internalB = layerQueryService.getLayer("testInternalBeanB")
        val internalC = layerQueryService.getLayer("testInternalBeanC")
        val leaf = layerQueryService.getLayer("testLeafBean")

        // then
        assertEquals(root.layer, 0)
        assertEquals(internalA.layer, 1)
        assertEquals(internalB.layer, 1)
        assertEquals(internalC.layer, 1)
        assertEquals(leaf.layer, 2)
    }

}
