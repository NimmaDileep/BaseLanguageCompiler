package edu.udel.blc.ast


class ArrayLiteralNode(
    range: IntRange,
    val elements: List<ExpressionNode>,
) : ExpressionNode(range)