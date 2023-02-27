package org.junnikym.springfbp.service

import org.junnikym.springfbp.BeanWithLayer

interface LayerQueryService {

    fun get(): List<List<BeanWithLayer>>;

    /**
     *
     * @return layer number
     */
    fun getLayer(beanName: String): Int

}