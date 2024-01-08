package org.junnikym.springfbp

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.springframework.aop.framework.AopProxyUtils
import java.lang.reflect.Method
import java.time.LocalDateTime
import java.util.*

@JsonSerialize(using = BeanEvent.BeanEventSerializer::class)
data class BeanEvent(
    val bean: Any,
    val from: BeanEvent? = null,
    val to: MutableList<BeanEvent> = mutableListOf(),
    val method: Method,
    val executedAt: LocalDateTime = LocalDateTime.now(),
    var finishedAt: LocalDateTime? = null
) {
    override fun toString(): String {
        return """BeanEvent(
            bean=$bean, 
            from=$from, 
            method=$method, 
            executedAt=$executedAt
            finishedAt=$finishedAt
        )
        """
    }

    fun toMetastasis(): Metastasis {
        val executedStack = Stack<BeanEvent>()

        var cur: BeanEvent? = this
        while(true) {
            if(cur == null)
                break

            executedStack.push(cur);
            cur = cur.from
        }

        val rootMetastasis = Metastasis.of(executedStack.pop())
        var lastMetastasis = rootMetastasis
        while(true) {
            val to = try { executedStack.pop() } catch (ex: EmptyStackException) { null } ?: break

            lastMetastasis.to = Metastasis.of(to)
            lastMetastasis = lastMetastasis.to!!
        }

        return rootMetastasis
    }

    data class Metastasis(
        val method: MethodExpr,
        var to: Metastasis?
    ) {
        companion object {
            fun of(from: BeanEvent) = Metastasis(
                method = from.method.let(MethodExpr::of),
                to = null
            )
        }
    }

    class BeanEventSerializer: JsonSerializer<BeanEvent>() {

        override fun serialize(value: BeanEvent?, gen: JsonGenerator?, serializers: SerializerProvider?) {
            gen?.writeStartObject()
            serializers?.defaultSerializeField("bean", value?.bean?.let(BeanExpr::of), gen)
            serializers?.defaultSerializeField("to", value?.to, gen)
            serializers?.defaultSerializeField("method", value?.method?.let(MethodExpr::of), gen)
            serializers?.defaultSerializeField("executedAt", value?.executedAt, gen)
            serializers?.defaultSerializeField("finishedAt", value?.finishedAt, gen)
            gen?.writeEndObject()
        }

    }

    data class FromExpr(
        val className: String,
        val methodName: String,
    ) {
        companion object {
            fun of(event: BeanEvent): FromExpr {
                val cls = AopProxyUtils.ultimateTargetClass(event.bean)
                return FromExpr(
                    className = cls.name,
                    methodName = event.method.name,
                )
            }
        }
    }

    data class BeanExpr(
        val className: String,
        val classSimpleName: String
    ) {
        companion object {
            fun of(obj: Any): BeanExpr {
                val cls = AopProxyUtils.ultimateTargetClass(obj)
                return BeanExpr(cls.name, cls.simpleName)
            }
        }
    }

    data class MethodExpr (
        val name: String,
        val className: String,
        val returnType: String,
    ) {
        companion object {
            fun of(method: Method): MethodExpr {
                return MethodExpr(
                    name = method.name,
                    className = method.declaringClass.name,
                    returnType = method.returnType.name,
                )
            }
        }
    }

}