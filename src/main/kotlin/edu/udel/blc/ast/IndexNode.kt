package edu.udel.blc.ast


class IndexNode(
    range: IntRange,
    val expression: ExpressionNode,
    val index: ExpressionNode,
) : ExpressionNode(range)