package org.junnikym.springfbp

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class DependencyFlowViewController {

    @RequestMapping
    fun getDependencyFlowView(model: Model): String {
        return "index";
    }

}