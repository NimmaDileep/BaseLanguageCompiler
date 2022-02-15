package edu.udel.blc.ast


class FieldNode(
    range: IntRange,
    val name: String,
    val type: Node,
) : Node(range)