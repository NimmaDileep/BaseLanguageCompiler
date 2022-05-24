package edu.udel.blc.ast


class FunctionDeclarationNode(
    range: IntRange,
    val name: String,
    val parameters: List<ParameterNode>,
    val returnType: Node?,
    val body: BlockNode,
) : StatementNode(range)