package edu.udel.blc.ast


class BooleanLiteralNode(
    range: IntRange,
    val value: Boolean,
) : ExpressionNode(range)