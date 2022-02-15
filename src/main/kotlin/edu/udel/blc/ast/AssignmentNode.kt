package edu.udel.blc.ast


class AssignmentNode(
    range: IntRange,
    val lvalue: ExpressionNode,
    val expression: ExpressionNode,
) : ExpressionNode(range)