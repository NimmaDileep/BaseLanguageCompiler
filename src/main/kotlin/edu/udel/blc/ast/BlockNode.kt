package edu.udel.blc.ast


class BlockNode(
    range: IntRange,
    val statements: List<StatementNode>,
) : StatementNode(range)