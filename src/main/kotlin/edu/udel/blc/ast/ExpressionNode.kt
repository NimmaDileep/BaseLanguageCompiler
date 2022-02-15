package edu.udel.blc.ast


sealed class ExpressionNode(
    range: IntRange,
) : Node(range)