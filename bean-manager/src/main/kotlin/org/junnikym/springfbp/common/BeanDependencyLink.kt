package org.junnikym.springfbp.common

data class BeanDependencyLink(
    val to: BeanDependencyNode,
    val from: BeanDependencyNode,
) {

    override fun toString(): String {
        return "[link] $from >>>>> $to";
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BeanDependencyLink

        return (from == other.to && to == other.to)
    }

    override fun hashCode(): Int {
        return 31 * from.hashCode() + to.hashCode()
    }

}