package edu.udel.blc.ast


class BinaryExpressionNode(
    range: IntRange,
    val operator: BinaryOperator,
    val left: ExpressionNode,
    val right: ExpressionNode,
) : ExpressionNode(range)