package org.junnikym.springfbp

import org.springframework.stereotype.Component

@Component
class ScriptInterpreterFacade {

    private val interpreter = mapOf (
        ScriptLanguage.Java to JavaScriptInterpreter(),
        ScriptLanguage.Kotlin to KotlinScriptInterpreter(),
    )

    fun eval(code: String, lang: ScriptLanguage) {
        interpreter[lang]!!.eval(code)
    }

}