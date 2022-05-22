package edu.udel.blc.ast

class ClassDeclarationNode(
    range: IntRange,
    val name: String,
    val fields: List<FieldNode>,
    val methods: List<FunctionDeclarationNode>,
    val superClass: String? = null,
) : StatementNode(range)
