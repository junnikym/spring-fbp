package org.junnikym.springfbp.beans

import org.springframework.stereotype.Component

@Component
class DummyBeanA ( private val dummyBeanB: DummyBeanB ) {
}
