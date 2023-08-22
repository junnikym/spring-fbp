package org.junnikym.springfbp.common

class BeanDependencyLink {

    val from: BeanDependencyNode;
    val to: BeanDependencyNode;

    constructor(
            from : BeanDependencyNode,
            to: BeanDependencyNode
    ) {
        this.from = from;
        this.to   = to;
    }

    override fun toString(): String {
        return "[link] $from >>>>> $to";
    }

}