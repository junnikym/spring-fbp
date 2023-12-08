package org.junnikym.springfbp.service

import org.junnikym.springfbp.BeanWithLayer
import org.junnikym.springfbp.BeanLayer
import org.junnikym.springfbp.filter.IgnoreManage

@IgnoreManage
interface LayerQueryService {

    fun get(): List<List<BeanWithLayer>>;

    /**
     *
     * @return layer number
     */
    fun getLayer(beanName: String): BeanLayer

}