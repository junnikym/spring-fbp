package org.junnikym.springfbp

import java.lang.reflect.Method
import javax.script.CompiledScript
import org.jetbrains.kotlin.cli.common.repl.CompiledClassData
import org.jetbrains.kotlin.cli.common.repl.KotlinJsr223JvmScriptEngineBase
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import kotlin.reflect.jvm.kotlinFunction

class KotlinScriptInterpreter: ScriptInterpreter() {

    private val kotlinScriptEngine =
        KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine as KotlinJsr223JvmLocalScriptEngine

    override fun eval(code: String) {
        try {
            kotlinScriptEngine.compile(code)
                .also(::putResources)
                .let(CompiledScript::eval)
        } catch (e: Exception) {
            throw ScriptRuntimeException(e)
        }
    }



    private fun putResources(compiledScript: CompiledScript) {
        putMethod(compiledScript)
        putClass(compiledScript)
    }

    private fun putClass(compiledScript: CompiledScript) {

        val classLoader = BytecodeClassLoader()
        val compiledClassData =
            (compiledScript as KotlinJsr223JvmScriptEngineBase.CompiledKotlinScript).compiledData

        compiledClassData.classes
            .filter { compiledClassData.mainClassName != it.path.let(::pathToClassName) }
            .mapNotNull (classLoader::defineClass)
            .map { getOriginalClassName(it.name) }
            .onEach(::checkClass)
            .forEach(::addClass)
    }

    private fun putMethod(compiledScript: CompiledScript) {
        val classLoader = BytecodeClassLoader()
        val compiledClassData =
            (compiledScript as KotlinJsr223JvmScriptEngineBase.CompiledKotlinScript).compiledData

        compiledClassData.classes
            .first { compiledClassData.mainClassName == it.path.let(::pathToClassName) }
            .let (classLoader::defineClass)
            ?.declaredMethods
            ?.mapNotNull { getSignature(it) }
            ?.onEach(::checkMethod)
            ?.forEach(::addMethod)
    }



    private fun pathToClassName(it: String): String {
        return it.replace(".class", "")
    }

    private fun getOriginalClassName(compiledClassName: String): String {
        return compiledClassName.split("$").last()
    }

    private fun getSignature(method: Method): String? {
        val ktFun = method.kotlinFunction ?: return null
        return ktFun::class.java.declaredFields
            .first { cls-> cls.name == "signature" }
            .also { it.isAccessible = true }
            .let { it.get(ktFun) } as String
    }



    private inner class BytecodeClassLoader : ClassLoader(ClassLoader.getSystemClassLoader()) {

        fun defineClass(compiledScriptClassData: CompiledClassData): Class<*>? {
            val name = compiledScriptClassData.path.let(::pathToClassName)
            val bytecode = compiledScriptClassData.bytes

            try {
                return defineClass(name, bytecode, 0, bytecode.size)
            } catch (e: Throwable) {
                return null
            }
        }

    }

}