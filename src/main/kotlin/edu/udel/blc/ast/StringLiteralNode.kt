package edu.udel.blc.ast


class StringLiteralNode(
    range: IntRange,
    val value: String,
) : ExpressionNode(range)