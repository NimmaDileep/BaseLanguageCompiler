package edu.udel.blc.ast


class ArrayTypeNode(
    range: IntRange,
    val elementType: Node,
) : Node(range)