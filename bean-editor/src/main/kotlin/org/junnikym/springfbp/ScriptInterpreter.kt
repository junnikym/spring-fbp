package org.junnikym.springfbp

interface ScriptInterpreter {

    fun eval(code: String)

    fun getMethodSignatures(): Set<String>

}