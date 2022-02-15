package edu.udel.blc.ast


class WhileNode(
    range: IntRange,
    val condition: ExpressionNode,
    val body: StatementNode,
) : StatementNode(range)