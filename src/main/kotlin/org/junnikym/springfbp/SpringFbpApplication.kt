package org.junnikym.springfbp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@SpringBootApplication
open class SpringFbpApplication

fun main(args: Array<String>) {
	runApplication<SpringFbpApplication>(*args)
}
