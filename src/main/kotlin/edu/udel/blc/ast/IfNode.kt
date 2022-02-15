package edu.udel.blc.ast


class IfNode(
    range: IntRange,
    val condition: ExpressionNode,
    val thenStatement: StatementNode,
    val elseStatement: StatementNode?,
) : StatementNode(range)