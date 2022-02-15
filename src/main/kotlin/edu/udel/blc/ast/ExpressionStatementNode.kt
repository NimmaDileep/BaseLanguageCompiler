package edu.udel.blc.ast


class ExpressionStatementNode(
    range: IntRange,
    val expression: ExpressionNode,
) : StatementNode(range)