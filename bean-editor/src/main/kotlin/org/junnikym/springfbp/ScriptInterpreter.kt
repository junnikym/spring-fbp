package org.junnikym.springfbp

abstract class ScriptInterpreter {

    companion object {
        protected val methodSignatures = mutableSetOf<String>()
        protected val classNames = mutableSetOf<String>()
    }

    abstract fun eval(code: String)


    protected fun addMethod(signature: String) = methodSignatures.add(signature)

    protected fun addClass(name: String) = classNames.add(name)

    fun getMethodSignatures(): Set<String> = methodSignatures.toSet()

    fun getClassNames(): Set<String> = classNames.toSet()

    protected fun checkMethod(signature: String) {
        if(methodSignatures.contains(signature))
            throw ScriptRuntimeException("Already declared method `$signature`")
    }

    protected fun checkClass(className: String) {
        if(classNames.contains(className))
            throw ScriptRuntimeException("Already declared class `$className`")
    }

}