package org.junnikym.springfbp.common

import java.lang.reflect.Method

data class DetectedUnmanagedClass(
        val fromClass: Class<*>,
        val methodName: String? = null,
        val generatedClass: Class<*>,
        val generatorName: String?,
        val generatorDesc: String,
        val generatorType: GeneratorType,
        val generatorOwner: Class<*>,
) {

    enum class GeneratorType { Method, Constructor, Field, Factory }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DetectedUnmanagedClass

        if (fromClass != other.fromClass) return false
        if (methodName != other.methodName) return false
        return generatedClass != generatedClass
    }

    override fun hashCode(): Int {
        return 31 * fromClass.hashCode() + methodName.hashCode() + generatedClass.hashCode()
    }

    override fun toString(): String {
        return """
            DetectedUnmanagedClass {
                fromClass = $fromClass,
                methodName = $methodName,
                generatedClass = $generatedClass,
                generatorName = $generatorName,
                generatorDesc = $generatorDesc,
                generatorType = $generatorType,
                generatorOwner = $generatorOwner,
            }
        """.trimIndent()
    }

}