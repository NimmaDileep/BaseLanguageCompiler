package edu.udel.blc.ast


class ReferenceNode(
    range: IntRange,
    val name: String,
) : ExpressionNode(range)