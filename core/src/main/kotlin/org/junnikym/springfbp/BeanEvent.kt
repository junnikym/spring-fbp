package org.junnikym.springfbp

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.lang.reflect.Method
import java.time.LocalDateTime
import java.util.UUID

@JsonSerialize(using = BeanEventSerializer::class)
data class BeanEvent(
    val id: UUID = UUID.randomUUID(),
    val bean: Any,
    var from: BeanEvent? = null,
    val to: MutableList<BeanEvent> = mutableListOf(),
    val method: Method,
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    override fun toString(): String {
        return """BeanEvent(
            id=$id,
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
        serializers?.defaultSerializeField("id", value?.id, gen)
        serializers?.defaultSerializeField("bean", value?.bean?.let(::BeanWrapper), gen)
        serializers?.defaultSerializeField("to", value, gen)
        serializers?.defaultSerializeField("method", value?.method?.let(::MethodWrapper), gen)
        serializers?.defaultSerializeField("createdAt", value?.createdAt, gen)
        gen?.writeEndObject()
    }

    data class BeanWrapper(
            val className: String,
            val classSimpleName: String
    ) {
        constructor(obj: Any) : this(
                className = obj::class.qualifiedName!!,
                classSimpleName = obj::class.simpleName!!,
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