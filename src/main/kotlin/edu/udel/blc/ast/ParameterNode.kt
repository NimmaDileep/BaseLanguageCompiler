package edu.udel.blc.ast


class ParameterNode(
    range: IntRange,
    val name: String,
    val type: Node,
) : Node(range)