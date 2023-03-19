package org.junnikym.springfbp

data class BeanWithLayer(
    val beanName: String,
    val linkedWith: List<String>,
    val layer: Int,
)
