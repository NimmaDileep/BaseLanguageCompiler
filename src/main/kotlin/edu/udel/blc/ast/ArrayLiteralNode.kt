package edu.udel.blc.ast


class ArrayLiteralNode(
    range: IntRange,
    val elements: List<ExpressionNode>,
) : ExpressionNode(range) {

    init {
        require(elements.isNotEmpty()) { "must contain at least one element" }
    }

}