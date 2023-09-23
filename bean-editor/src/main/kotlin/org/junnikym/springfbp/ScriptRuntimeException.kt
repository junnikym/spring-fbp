package org.junnikym.springfbp

class ScriptRuntimeException(message: String? = null): RuntimeException(message) {
    constructor(e: Exception): this(e.message)
}