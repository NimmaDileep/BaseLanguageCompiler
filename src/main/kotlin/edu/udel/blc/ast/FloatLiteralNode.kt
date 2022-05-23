package edu.udel.blc.ast

class FloatLiteralNode (
    range: IntRange,
    val value: Float,
    ) : ExpressionNode(range)