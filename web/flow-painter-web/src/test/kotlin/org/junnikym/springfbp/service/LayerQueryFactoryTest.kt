package org.junnikym.springfbp.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junnikym.springfbp.beans.TestBeanDependencyLinkFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class LayerQueryFactoryTest(
    @Autowired beanFactory: ConfigurableListableBeanFactory,
    @Autowired beanLayerFactory: BeanLayerFactory
) {

    private val beanLayerFactory: BeanLayerFactory;

    init {
        val linkFactory = TestBeanDependencyLinkFactory(beanFactory)
        this.beanLayerFactory = beanLayerFactory
            .javaClass
            .constructors[0]
            .newInstance(linkFactory) as BeanLayerFactory;

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
