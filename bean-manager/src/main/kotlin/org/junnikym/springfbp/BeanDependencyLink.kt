package org.junnikym.springfbp

class BeanDependencyLink (_from: Any?, _to: Any?) {

    val from = _from;
    val to   = _to;

    override fun toString(): String {
        return "[link] $from >>>>> $to";
    }

}