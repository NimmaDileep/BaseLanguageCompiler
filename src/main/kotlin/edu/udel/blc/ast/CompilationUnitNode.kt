package edu.udel.blc.ast


class CompilationUnitNode(
    range: IntRange,
    val statements: List<StatementNode>,
) : Node(range)