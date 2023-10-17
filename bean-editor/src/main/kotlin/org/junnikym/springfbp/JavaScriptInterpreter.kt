package org.junnikym.springfbp

import java.util.*
import jdk.jshell.Diag
import jdk.jshell.JShell
import jdk.jshell.MethodSnippet
import jdk.jshell.Snippet
import jdk.jshell.SnippetEvent
import jdk.jshell.TypeDeclSnippet
import jdk.jshell.execution.LocalExecutionControlProvider

class JavaScriptInterpreter: ScriptInterpreter() {

    private val scriptInterpreterClass = ScriptInterpreter::class.java

    private val jshell = JShell.builder()
        .executionEngine(LocalExecutionControlProvider(), null)
        .build()

    init {
        runCodeOnShell("import ${scriptInterpreterClass.name}")
    }

    override fun eval(code: String) {
        runCodeOnShell(code)?.let {
            it.snippets.forEach(::putResources)
            it.remainingCode.let(::eval)
        }
    }

    private fun runCodeOnShell(code: String): ShellResult? {
        if(code.isBlank())
            return null

        val completionInfo = jshell.sourceCodeAnalysis().analyzeCompletion(code)
        val source = completionInfo.source()

        val snippetEvents = try { jshell.eval(source) } catch (e: Exception) { throw ScriptRuntimeException(e) }
        snippetEvents.forEach { event->
            jshell.diagnostics(event.snippet())
                .toList()
                .find(Diag::isError)
                ?.let { throw getErrorMessageInJshell(source, it).exceptionOf() }
        }

        return ShellResult(
            snippets = snippetEvents,
            remainingCode = completionInfo.remaining(),
        )
    }



    private fun putResources(event: SnippetEvent) {
        val snippet = event.snippet()
        putMethod(snippet)
        putClass(snippet)
    }

    private fun putMethod(snippet: Snippet) {
        if(snippet.kind() != Snippet.Kind.METHOD)
            return

        getDescriptorStringOfSignature(snippet as MethodSnippet)
            .also(::checkMethod)
            .let(::addMethod)
    }

    private fun putClass(snippet: Snippet) {
        if(snippet.kind() != Snippet.Kind.TYPE_DECL)
            return

        (snippet as TypeDeclSnippet).name().let(::addClass)
    }

    private fun getDescriptorStringOfSignature(methodSnippet: MethodSnippet): String {
        val signature = methodSnippet.signature()
        val name = methodSnippet.name()
        val parameter = methodSnippet.parameterTypes()
            .split(",")
            .map(::getDescriptorStringOfType)
            .reduce { acc, value -> acc + value }
        val returnType = signature
            .split(")").last()
            .let(::getDescriptorStringOfType)

        return "$name($parameter)$returnType"
    }

    private fun getDescriptorStringOfType (typename: String): String {
        return try {
            TypeClassFactory.of(typename).descriptorString()
        } catch (e: ClassNotFoundException) {
            "$typename.class.descriptorString()"
                .trimIndent()
                .let { runCodeOnShell(it)!!.snippets[0].value() }
                .let { it.replace("\"", "") }
        }
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

    private data class ShellResult(
        val snippets: List<SnippetEvent>,
        val remainingCode: String,
    )

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