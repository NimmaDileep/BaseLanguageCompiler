package edu.udel.blc.ast

class ClassDeclarationNode(
    range: IntRange,
    val name: String,
    val fields: List<FieldNode>,
) : StatementNode(range)
