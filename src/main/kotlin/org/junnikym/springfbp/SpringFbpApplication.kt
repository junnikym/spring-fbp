package org.junnikym.springfbp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class SpringFbpApplication

fun main(args: Array<String>) {
	runApplication<SpringFbpApplication>(*args)
}
