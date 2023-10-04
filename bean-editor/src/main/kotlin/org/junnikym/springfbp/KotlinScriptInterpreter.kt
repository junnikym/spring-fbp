package org.junnikym.springfbp

import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory

class KotlinScriptInterpreter: ScriptInterpreter {

    private val kotlinScriptEngine = KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine

    override fun eval(code: String) {
        try {
            kotlinScriptEngine.eval(code)
        } catch (e: Exception) {
            throw ScriptRuntimeException(e)
        }
    }

}