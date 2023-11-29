package org.junnikym.springfbp.controller

import org.junnikym.springfbp.BeanWithLayer
import org.junnikym.springfbp.BeanLayer
import org.junnikym.springfbp.service.LayerQueryService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/bean/layer")
class BeanLayerController(
    private val layerQueryService: LayerQueryService,
) {

    @GetMapping
    fun getBeanLayer(): List<List<BeanWithLayer>> {
        return layerQueryService.get()
    }

    @GetMapping("/{beanName}")
    fun getLayerByBeanName(@PathVariable beanName: String): BeanLayer {
        return layerQueryService.getLayer(beanName);
    }

}