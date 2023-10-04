package org.junnikym.springfbp

import java.util.*
import jdk.jshell.Diag
import jdk.jshell.JShell

class JavaScriptInterpreter: ScriptInterpreter {

    private val jshell = JShell.create()

    override fun eval(code: String) {
        if(code.isBlank())
            return

        val completionInfo = jshell.sourceCodeAnalysis().analyzeCompletion(code)
        val source = completionInfo.source()

        val snippetEvents = try { jshell.eval(source) } catch (e: Exception) { throw ScriptRuntimeException(e) }
        snippetEvents.forEach eventLoop@{ event->
            val diag = jshell.diagnostics(event.snippet()).toList().find(Diag::isError) ?: return@eventLoop
            throw getErrorMessageInJshell(source, diag).exceptionOf()
        }

        eval(completionInfo.remaining())
    }

    private fun getErrorMessageInJshell(source: String, diag: Diag): ErrorMessage {
        val message = ErrorMessage(diag.getMessage(Locale.ENGLISH))
        findErrorCodeLineInJshell(source, diag.position, message)
        return message
    }

    private fun findErrorCodeLineInJshell(source: String, position: Long, message: ErrorMessage) {
        var cursor = 0
        source.split("\n").forEach loop@{ line ->
            cursor += line.length
            if(cursor < position) return@loop

            val pointer = (line.length-1 - (cursor - position)).let { " ".repeat(it.toInt()) + "^" }
            message.sourceLine = line
            message.sourceLinePointer = pointer
            return
        }

        return
    }

    private data class ErrorMessage(
        var errorMessage: String,
        var sourceLine: String? = null,
        var sourceLinePointer: String? = null,
    ) {
        override fun toString(): String {
            return """
                ScriptError:
                    $errorMessage
                    $sourceLine
                    $sourceLinePointer
            """.trimIndent()
        }

        fun exceptionOf(): ScriptRuntimeException {
            return ScriptRuntimeException(toString())
        }
    }

}