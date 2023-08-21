package org.junnikym.springfbp.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junnikym.springfbp.beans.TestFactoryProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(locations= ["classpath:test.properties"])
class LayerQueryServiceTest(
        @Autowired beanFactory: ConfigurableListableBeanFactory,
        @Autowired testFactoryProvider: TestFactoryProvider,
        @Autowired layerQueryService: LayerQueryService,
) {

    private val layerQueryService: LayerQueryService

    init {
        val layerFactory = testFactoryProvider.layerFactoryOf()

        this.layerQueryService = layerQueryService
            .javaClass
            .constructors[0]
            .newInstance(
                    beanFactory,
                    testFactoryProvider.linkFactoryOf(),
                    testFactoryProvider.nodeFactoryOf(),
                    layerFactory,
            ) as LayerQueryService

        layerFactory.update()
    }

    @Test
    fun get_all_beans_with_layer_which_organized_by_layer() {
        // given

        // when
        val beansWithLayer = layerQueryService.get()

        println(beansWithLayer)

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
        assertEquals(root, 0)
        assertEquals(internalA, 1)
        assertEquals(internalB, 1)
        assertEquals(internalC, 1)
        assertEquals(leaf, 2)
    }

}
