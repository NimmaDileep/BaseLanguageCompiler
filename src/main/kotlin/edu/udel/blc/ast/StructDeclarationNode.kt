package edu.udel.blc.ast


class StructDeclarationNode(
    range: IntRange,
    val name: String,
    val fields: List<FieldNode>,
) : StatementNode(range)