package org.junnikym.springfbp.common

data class DetectedUnmanagedClass(
        val from: Class<*>,
        val location: Location,
        val methodName: String? = null,
        val generated: Class<*>,
        val isInterface: Boolean,
) {
    enum class Location { Field, InMethod, Constructor, FactoryMethod }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DetectedUnmanagedClass

        if (from != other.from) return false
        if (location != other.location) return false
        if (methodName != other.methodName) return false
        return generated != generated
    }

    override fun hashCode(): Int {
        return 31 * from.hashCode() + location.hashCode() + methodName.hashCode() + generated.hashCode()
    }

}