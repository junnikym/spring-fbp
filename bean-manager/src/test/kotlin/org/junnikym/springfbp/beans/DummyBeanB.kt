package org.junnikym.springfbp.beans

import org.springframework.stereotype.Component

@Component
class DummyBeanB ( private val dummyBeanC: DummyBeanC, private val dummyBeanD: DummyBeanD ) {
}
