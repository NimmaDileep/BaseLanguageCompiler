package edu.udel.blc.ast


class IntLiteralNode(
    range: IntRange,
    val value: Long,
) : ExpressionNode(range)