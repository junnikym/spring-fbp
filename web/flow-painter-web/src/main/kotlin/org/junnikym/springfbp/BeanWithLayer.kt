package org.junnikym.springfbp

import kotlin.reflect.KClass

data class BeanWithLayer (
    val beanName: String,
    val beanClassQualifiedName: String,
    val beanClassSimpleName: String,
    val linkedWith: List<LinkedBean>,
    val layer: Int,
) {
    data class LinkedBean (
            val beanName: String,
            val beanClassQualifiedName: String,
            val beanClassSimpleName: String,
    )
}
