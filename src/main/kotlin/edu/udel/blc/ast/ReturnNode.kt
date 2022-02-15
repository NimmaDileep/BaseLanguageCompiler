package edu.udel.blc.ast


class ReturnNode(
    range: IntRange,
    val expression: ExpressionNode?,
) : StatementNode(range)