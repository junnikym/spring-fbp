package org.junnikym.springfbp.beans

import org.springframework.stereotype.Component

@Component
class TestRootBean(
    private val childA: TestInternalBeanA,
    private val childB: TestInternalBeanB,
    private val childC: TestInternalBeanC,
) {

}
