package org.junnikym.springfbp

import java.lang.reflect.Method
import javax.script.CompiledScript
import org.jetbrains.kotlin.cli.common.repl.CompiledClassData
import org.jetbrains.kotlin.cli.common.repl.KotlinJsr223JvmScriptEngineBase
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.kotlinFunction

class KotlinScriptInterpreter: ScriptInterpreter {

    private val kotlinScriptEngine =
        KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine as KotlinJsr223JvmLocalScriptEngine

    private val methods = mutableMapOf<String, Method>()

    private val classes = mutableMapOf<String, Class<*>>()

    override fun eval(code: String) {
        try {
            kotlinScriptEngine.compile(code)
                .also(::putClassMap)
                .also(::putMethodMap)
                .let(CompiledScript::eval)
        } catch (e: Exception) {
            throw ScriptRuntimeException(e)
        }
    }

    override fun getMethodSignatures(): Set<String> {
        return methods.keys
    }

    override fun getClassNames(): Set<String> {
        return classes.keys
    }



    private fun putClassMap(compiledScript: CompiledScript) {
        val classLoader = BytecodeClassLoader()
        val compiledClassData =
            (compiledScript as KotlinJsr223JvmScriptEngineBase.CompiledKotlinScript).compiledData

        compiledClassData.classes
            .filter { compiledClassData.mainClassName != it.path.let(::pathToClassName) }
            .mapNotNull (classLoader::defineClass)
            .map { getOriginalClassName(it.name) to it }
            .onEach { pair-> checkClass(pair.first) }
            .forEach { pair-> classes[pair.first] = pair.second }
    }

    private fun putMethodMap(compiledScript: CompiledScript) {
        val classLoader = BytecodeClassLoader()
        val compiledClassData =
            (compiledScript as KotlinJsr223JvmScriptEngineBase.CompiledKotlinScript).compiledData

        compiledClassData.classes
            .first { compiledClassData.mainClassName == it.path.let(::pathToClassName) }
            .let (classLoader::defineClass)
            ?.declaredMethods
            ?.mapNotNull (::getSignatureAndMethod)
            ?.onEach { pair-> checkMethod(pair.first) }
            ?.forEach { pair-> methods[pair.first] = pair.second  }
    }



    private fun pathToClassName(it: String): String {
        return it.replace(".class", "")
    }

    private fun getOriginalClassName(compiledClassName: String): String {
        return compiledClassName.split("$").last()
    }


    private fun getSignatureAndMethod(method: Method): Pair<String, Method>? {
        val ktFun = method.kotlinFunction ?: return null
        return getSignature(ktFun) to method
    }

    private fun getSignature(function: KFunction<*>): String {
        return function::class.java.declaredFields
            .first { cls-> cls.name == "signature" }
            .also { it.isAccessible = true }
            .let { it.get(function) } as String
    }



    private fun checkClass(className: String) {
        if(classes.contains(className))
            throw ScriptRuntimeException("Already declared class `$className`")
    }

    private fun checkMethod(signature: String) {
        if(methods.contains(signature))
            throw ScriptRuntimeException("Already declared method `$signature`")
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