package org.junnikym.springfbp

import org.junnikym.springfbp.filter.IgnoreManage
import org.junnikym.springfbp.service.LayerQueryService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@IgnoreManage
class DependencyFlowViewController(
    private val layerQueryService: LayerQueryService,
) {

    @RequestMapping
    fun getDependencyFlowView(model: Model): String {
        val beansWithLayer = layerQueryService.get()
        model.addAttribute("beansWithLayer", beansWithLayer)
        return "index"
    }

}
