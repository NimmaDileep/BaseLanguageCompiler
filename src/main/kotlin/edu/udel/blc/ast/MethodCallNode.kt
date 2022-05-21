package edu.udel.blc.ast

class MethodCallNode(
    range: IntRange,
    val callee: String,
    val arguments: List<ExpressionNode>,
    val receiver: ExpressionNode
) : ExpressionNode(range)
