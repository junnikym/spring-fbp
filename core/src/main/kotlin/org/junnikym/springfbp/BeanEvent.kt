package org.junnikym.springfbp

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.lang.reflect.Method
import java.time.LocalDateTime

@JsonSerialize(using = BeanEventSerializer::class)
data class BeanEvent(
        val bean: Any,
        val from: StackTraceElement?,
        val method: Method,
        val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    override fun toString(): String {
        return """BeanEvent(
            bean=$bean, 
            from=$from, 
            method=$method, 
            createdAt=$createdAt
        )
        """
    }
}

class BeanEventSerializer: JsonSerializer<BeanEvent>() {

    override fun serialize(value: BeanEvent?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeStartObject()
        serializers?.defaultSerializeField("bean", value?.bean?.let(::BeanWrapper), gen)
        serializers?.defaultSerializeField("from", value?.from?.let(::FromWrapper), gen)
        serializers?.defaultSerializeField("method", value?.method?.let(::MethodWrapper), gen)
        serializers?.defaultSerializeField("createdAt", value?.createdAt, gen)
        gen?.writeEndObject()
    }

    data class BeanWrapper(
            val className: String
    ) {
        constructor(obj: Any) : this(
                className = obj::class.qualifiedName!!
        )
    }

    data class FromWrapper(
            val className: String,
            val methodName: String,
            val lineNumber: Int,
    ) {
        constructor(stackTrace: StackTraceElement) : this(
                className = stackTrace.className,
                methodName = stackTrace.methodName,
                lineNumber = stackTrace.lineNumber,
        )
    }

    data class MethodWrapper (
            val name: String,
            val className: String,
            val returnType: String,
    ) {
        constructor(method: Method) : this(
                name = method.name,
                className = method.declaringClass.name,
                returnType = method.returnType.name,
        )
    }

}