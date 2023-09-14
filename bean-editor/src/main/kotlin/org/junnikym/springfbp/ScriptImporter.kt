package org.junnikym.springfbp

import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import org.springframework.stereotype.Component
import javax.script.ScriptEngine

@Component
class ScriptImporter {

    private val kotlinScriptEngine: ScriptEngine = KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine

    fun runKotlinScript(code: String) {
        try {
            kotlinScriptEngine.eval(code)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

}