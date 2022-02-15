package edu.udel.blc.ast


class UnaryExpressionNode(
    range: IntRange,
    val operator: UnaryOperator,
    val operand: ExpressionNode,
) : ExpressionNode(range)