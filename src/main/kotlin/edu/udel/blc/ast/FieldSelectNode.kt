package edu.udel.blc.ast


class FieldSelectNode(
    range: IntRange,
    val expression: ExpressionNode,
    val name: String,
) : ExpressionNode(range)