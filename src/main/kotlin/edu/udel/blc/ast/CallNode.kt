package edu.udel.blc.ast


class CallNode(
    range: IntRange,
    val callee: ExpressionNode,
    val arguments: List<ExpressionNode>,
) : ExpressionNode(range)