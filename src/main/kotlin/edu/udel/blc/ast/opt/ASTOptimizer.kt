package edu.udel.blc.ast.opt

import edu.udel.blc.ast.*
import edu.udel.blc.ast.BinaryOperator.*
import edu.udel.blc.ast.UnaryOperator.*
import edu.udel.blc.util.visitor.ValuedVisitor
import jdk.incubator.vector.VectorOperators.Binary

class ExpressionOptimizer : ValuedVisitor<Node, Node>() {

    init {
        register(ArrayLiteralNode::class.java, ::arrayLiteral)
        register(AssignmentNode::class.java, ::assignment)
        register(BinaryExpressionNode::class.java, ::binaryExpression)
        register(BlockNode::class.java, ::block)
        register(CallNode::class.java, ::call)
        register(CompilationUnitNode::class.java, ::compilationUnit)
        register(ExpressionStatementNode::class.java, ::expressionStatement)
        register(FunctionDeclarationNode::class.java, ::functionDeclaration)
        register(IfNode::class.java, ::ifStmt)
        register(IndexNode::class.java, ::index)
        register(ReturnNode::class.java, ::returnStmt)
        register(UnaryExpressionNode::class.java, ::unaryExpression)
        register(VariableDeclarationNode::class.java, ::variableDeclaration)
        register(WhileNode::class.java, ::whileStmt)

        registerFallback(::identity)
    }

    private fun identity(node: Node): Node = node

    private fun arrayLiteral(node: ArrayLiteralNode): ArrayLiteralNode = ArrayLiteralNode(
        range = node.range,
        elements = node.elements.map { apply(it) as ExpressionNode }
    )

    private fun assignment(node: AssignmentNode): AssignmentNode = AssignmentNode(
        range = node.range,
        lvalue = apply(node.lvalue) as ExpressionNode,
        expression = apply(node.expression) as ExpressionNode
    )

    private fun binaryExpression(node: BinaryExpressionNode): Node {
        return when (node.operator) {
            ADDITION -> addition(node)
            SUBTRACTION -> subtraction(node)
            MULTIPLICATION -> multiplication(node)
            REMAINDER -> remainder(node)
            else -> node
//            EQUAL_TO -> TODO()
//            NOT_EQUAL_TO -> TODO()
//            GREATER_THAN -> TODO()
//            GREATER_THAN_OR_EQUAL_TO -> TODO()
//            LESS_THAN -> TODO()
//            LESS_THAN_OR_EQUAL_TO -> TODO()
//            LOGICAL_CONJUNCTION -> TODO()
//            LOGICAL_DISJUNCTION -> TODO()
        }
    }

    private fun addition(node: BinaryExpressionNode): Node {
        val left = apply(node.left) as ExpressionNode
        val right = apply(node.right) as ExpressionNode
        return when {
            left is IntLiteralNode && (right is IntLiteralNode && right.value == 0L)-> {
                IntLiteralNode(
                    range = left.range.first..right.range.last,
                    value = left.value
                )
            }
            (left is IntLiteralNode && left.value == 0L) && right is IntLiteralNode -> {
                IntLiteralNode(
                    range = left.range.first..right.range.last,
                    value = right.value
                )
            }
            left is IntLiteralNode && right is IntLiteralNode -> {
                IntLiteralNode(
                    range = left.range.first..right.range.last,
                    value = left.value + right.value
                )
            }
            else -> {
                BinaryExpressionNode(
                    range = node.range,
                    operator = node.operator,
                    left = left,
                    right = right
                )
            }
        }
    }

    private fun subtraction(node: BinaryExpressionNode): Node {
        val left = apply(node.left) as ExpressionNode
        val right = apply(node.right) as ExpressionNode

        return when {
            left is IntLiteralNode && (right is IntLiteralNode && right.value == 0L)-> {
                IntLiteralNode(
                    range = left.range.first..right.range.last,
                    value = left.value
                )
            }
            left is IntLiteralNode && right is IntLiteralNode -> {
                IntLiteralNode(
                    range = left.range.first..right.range.last,
                    value = left.value - right.value
                )
            }
            else -> {
                BinaryExpressionNode(
                    range = node.range,
                    operator = node.operator,
                    left = left,
                    right = right
                )
            }
        }
    }

    private fun multiplication(node: BinaryExpressionNode): Node {
        val left = apply(node.left) as ExpressionNode
        val right = apply(node.right) as ExpressionNode

        return when {
            left is IntLiteralNode && (right is IntLiteralNode && right.value == 0L)-> {
                IntLiteralNode(
                    range = left.range.first..right.range.last,
                    value = 0
                )
            }
            (left is IntLiteralNode && left.value == 0L) && right is IntLiteralNode -> {
                IntLiteralNode(
                    range = left.range.first..right.range.last,
                    value = 0
                )
            }
            left is IntLiteralNode && (right is IntLiteralNode && right.value.toInt() == 1)-> {
                IntLiteralNode(
                    range = left.range.first..right.range.last,
                    value = left.value
                )
            }
            (left is IntLiteralNode && left.value == 1L) && right is IntLiteralNode -> {
                IntLiteralNode(
                    range = left.range.first..right.range.last,
                    value = right.value
                )
            }
            (left is IntLiteralNode && left.value == 2L) && right is ExpressionNode -> {
                ExpressionNode(
                    range = node.right
                )
            }
            left is IntLiteralNode && (right is IntLiteralNode && right.value.toInt() == 2)-> {
                IntLiteralNode(
                    range = left.range.first..right.range.last,
                    value = left.value + left.value
                )
            }
            left is IntLiteralNode && right is IntLiteralNode -> {
                IntLiteralNode(
                    range = left.range.first..right.range.last,
                    value = left.value * right.value
                )
            }
            else -> {
                BinaryExpressionNode(
                    range = node.range,
                    operator = node.operator,
                    left = left,
                    right = right
                )
            }
        }
    }


    private fun remainder(node: BinaryExpressionNode): Node {
        val left = apply(node.left) as ExpressionNode
        val right = apply(node.right) as ExpressionNode

        return when {
            left is IntLiteralNode && (right is IntLiteralNode && right.value != 0L)-> {
                IntLiteralNode(
                    range = left.range.first..right.range.last,
                    value = left.value % right.value
                )
            }
            else -> {
                BinaryExpressionNode(
                    range = node.range,
                    operator = node.operator,
                    left = left,
                    right = right
                )
            }
        }
    }

    private fun block(node: BlockNode): BlockNode = BlockNode (
        range = node.range,
        statements = node.statements.map { apply(it) as StatementNode }
    )
//    private fun booleanLiteral(node: BooleanLiteralNode): BooleanLiteralNode {}
    private fun call(node: CallNode): CallNode = CallNode (
        range = node.range,
        callee = apply(node.callee) as ExpressionNode,
        arguments = node.arguments.map { apply(it) as ExpressionNode }
    )
    private fun compilationUnit(node: CompilationUnitNode): CompilationUnitNode = CompilationUnitNode(
        range = node.range,
        statements = node.statements.map { apply(it) as StatementNode }
    )

    private fun expressionStatement(node: ExpressionStatementNode): ExpressionStatementNode = ExpressionStatementNode(
        range = node.range,
        expression = apply(node.expression) as ExpressionNode
    )
    private fun functionDeclaration(node: FunctionDeclarationNode): FunctionDeclarationNode = FunctionDeclarationNode(
        range = node.range,
        name = node.name,
        parameters = node.parameters,
        returnType = node.returnType,
        body = apply(node.body) as BlockNode
    )
    private fun ifStmt(node: IfNode): IfNode = IfNode(
        range = node.range,
        condition = apply(node.condition) as ExpressionNode,
        thenStatement = apply(node.thenStatement) as StatementNode,
        elseStatement = node.elseStatement?.let { apply(it) } as StatementNode?
    )
    private fun index(node: IndexNode): IndexNode  = IndexNode(
        range = node.range,
        expression = apply(node.expression) as ExpressionNode,
        index = apply(node.index) as ExpressionNode
    )
    private fun returnStmt(node: ReturnNode): ReturnNode = ReturnNode(
        range = node.range,
        expression = node.expression?.let { apply(it) } as ExpressionNode?
    )

    private fun unaryExpression(node: UnaryExpressionNode): UnaryExpressionNode {
        return when (node.operator) {
            else -> node
//            NEGATION -> TODO()
//            LOGICAL_COMPLEMENT -> TODO()
        }
    }

    private fun variableDeclaration(node: VariableDeclarationNode): VariableDeclarationNode = VariableDeclarationNode(
        range = node.range,
        name = node.name,
        type = node.type,
        initializer = apply(node.initializer) as ExpressionNode
    )

    private fun whileStmt(node: WhileNode): WhileNode = WhileNode(
        range = node.range,
        condition = apply(node.condition) as ExpressionNode,
        body = apply(node.body) as StatementNode
    )
}