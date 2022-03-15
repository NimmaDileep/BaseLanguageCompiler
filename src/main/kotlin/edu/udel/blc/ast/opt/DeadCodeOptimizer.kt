package edu.udel.blc.ast.opt

import edu.udel.blc.ast.*
import edu.udel.blc.util.visitor.ValuedVisitor

class DeadCodeOptimizer : ValuedVisitor<Node, Node>() {
    init {
        register(CompilationUnitNode::class.java, ::compilationUnit);
        register(BlockNode::class.java, ::block)
        register(FunctionDeclarationNode::class.java, ::functionDeclaration)
        register(IfNode::class.java, ::ifStmt)
        register(WhileNode::class.java, ::whileStmt)

        registerFallback(::identity)
    }

    private fun identity(node: Node): Node = node

    private fun compilationUnit(node: CompilationUnitNode): Node =
        CompilationUnitNode(range = node.range, statements = node.statements.map { apply(it) as StatementNode })

    private fun block(node: BlockNode): Node {
        var foundReturn = false

        return BlockNode(
            range = node.range,
            statements = node.statements.map {
                apply(it) as StatementNode
            }.takeWhile {
                val hadReturn = foundReturn
                if(it is ReturnNode) foundReturn = true
                !hadReturn
            }
        )
    }

    private fun functionDeclaration(node: FunctionDeclarationNode): Node = FunctionDeclarationNode(
        range = node.range,
        name = node.name,
        returnType = node.returnType,
        parameters = node.parameters,
        body = apply(node.body) as BlockNode
    )


    private fun ifStmt(node: IfNode): Node {
        return when (node.condition) {
            is BooleanLiteralNode -> when (node.condition.value) {
                true -> {
                    apply(node.thenStatement)
                }
                false -> node.elseStatement?.let {
                    apply(node.elseStatement)
                } ?: emptyNode(node)
            }
            else -> {
                IfNode(range = node.range,
                    condition = node.condition,
                    thenStatement = apply(node.thenStatement) as StatementNode,
                    elseStatement = node.elseStatement?.let { apply(it) } as StatementNode?)
            }
        }
    }

    private fun whileStmt(node: WhileNode): Node {
        return when {
            node.condition is BooleanLiteralNode && !node.condition.value -> emptyNode(node)
            else -> {
                WhileNode(
                    range = node.range, condition = node.condition, body = apply(node.body) as StatementNode
                )
            }
        }
    }

    private fun emptyNode(original: Node): Node {
        return ExpressionStatementNode(
            range = original.range, expression = UnitLiteralNode(
                range = original.range
            )
        )
    }
}