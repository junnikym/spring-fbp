package org.junnikym.springfbp.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junnikym.springfbp.beans.TestFactoryProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(locations= ["classpath:test.properties"])
class LayerQueryFactoryTest(
        @Autowired testFactoryProvider: TestFactoryProvider,
) {

    private val beanLayerFactory: BeanLayerFactory;

    init {
        this.beanLayerFactory = testFactoryProvider.layerFactoryOf()
        this.beanLayerFactory.update()
    }

    @Test
    fun get_bean_layers() {
        // given

        // when
        val rootLayer = this.beanLayerFactory.get("testRootBean");
        val internalALayer = this.beanLayerFactory.get("testInternalBeanA");
        val internalBLayer = this.beanLayerFactory.get("testInternalBeanB");
        val internalCLayer = this.beanLayerFactory.get("testInternalBeanC");
        val leafLayer = this.beanLayerFactory.get("testLeafBean");

        // then
        assertEquals(rootLayer, 0)
        assertEquals(internalALayer, 1)
        assertEquals(internalBLayer, 1)
        assertEquals(internalCLayer, 1)
        assertEquals(leafLayer, 2)
    }


}
