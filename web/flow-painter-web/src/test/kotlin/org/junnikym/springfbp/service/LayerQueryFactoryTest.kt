package org.junnikym.springfbp.service

import org.junit.jupiter.api.Test
import org.junnikym.springfbp.beans.TestBeanDependencyLinkFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

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
    }

}
