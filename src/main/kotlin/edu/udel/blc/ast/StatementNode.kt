package edu.udel.blc.ast


sealed class StatementNode(
    range: IntRange,
) : Node(range)