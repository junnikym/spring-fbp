package org.junnikym.springfbp.beans

import org.springframework.stereotype.Component

@Component
class TestInternalBeanA(
    private val child: TestLeafBean
) {
}
