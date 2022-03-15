package edu.udel.blc.ast.opt

import edu.udel.blc.ast.*
import edu.udel.blc.ast.BinaryOperator.*
import edu.udel.blc.ast.UnaryOperator.*
import edu.udel.blc.util.visitor.ValuedVisitor

class ExpressionOptimizer(
    val constantFolding: Boolean,
    val strengthReduction: Boolean
) : ValuedVisitor<Node, Node>() {

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
            EQUAL_TO -> equality(node)
            NOT_EQUAL_TO -> equality(node, true)
            GREATER_THAN -> comparison(node, GREATER_THAN)
            GREATER_THAN_OR_EQUAL_TO -> comparison(node, GREATER_THAN_OR_EQUAL_TO)
            LESS_THAN -> comparison(node, LESS_THAN)
            LESS_THAN_OR_EQUAL_TO -> comparison(node, LESS_THAN_OR_EQUAL_TO)
            LOGICAL_CONJUNCTION -> logicalAnd(node)
            LOGICAL_DISJUNCTION -> logicalOr(node)
        }
    }

    private fun addition(node: BinaryExpressionNode): Node {
        val left = apply(node.left) as ExpressionNode
        val right = apply(node.right) as ExpressionNode

        return when {
            constantFolding && left is IntLiteralNode && right is IntLiteralNode -> {
                IntLiteralNode(
                    range = node.range,
                    value = left.value + right.value
                )
            }
            strengthReduction && (left is IntLiteralNode && left.value == 0L) || (right is IntLiteralNode && right.value == 0L) -> {
                IntLiteralNode(
                    range = node.range,
                    value = 0L
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
            constantFolding && left is IntLiteralNode && right is IntLiteralNode -> {
                IntLiteralNode(
                    range = node.range,
                    value = left.value - right.value
                )
            }
            strengthReduction && right is IntLiteralNode && right.value == 0L -> left
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
            constantFolding && left is IntLiteralNode && right is IntLiteralNode -> {
                IntLiteralNode(
                    range = node.range,
                    value = left.value * right.value
                )
            }
            strengthReduction && (left is IntLiteralNode && left.value == 0L || right is IntLiteralNode && right.value == 0L) -> IntLiteralNode(
                range = node.range, value = 0L
            )
            strengthReduction && left is IntLiteralNode && left.value == 1L -> right
            strengthReduction && right is IntLiteralNode && right.value == 1L -> left
            // TODO: Add a * 2 strength reduction
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
            constantFolding && left is IntLiteralNode && (right is IntLiteralNode && right.value != 0L) -> {
                IntLiteralNode(
                    range = node.range,
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

    private fun equality(node: BinaryExpressionNode, negated: Boolean = false): Node {
        val left = apply(node.left) as ExpressionNode
        val right = apply(node.right) as ExpressionNode

        if(!constantFolding) return BinaryExpressionNode(
            range = node.range,
            operator = node.operator,
            left = left,
            right = right
        )

        return when {
            left is IntLiteralNode && right is IntLiteralNode -> {
                val eval = left.value == right.value
                BooleanLiteralNode(
                    range = node.range,
                    value = if (negated) !eval else eval
                )
            }
            left is BooleanLiteralNode && right is BooleanLiteralNode -> {
                val eval = left.value == right.value
                BooleanLiteralNode(
                    range = node.range,
                    value = if (negated) !eval else eval
                )
            }
            left is StringLiteralNode && right is StringLiteralNode -> {
                val eval = left.value == right.value
                BooleanLiteralNode(
                    range = node.range,
                    value = if (negated) !eval else eval
                )
            }
            left is UnitLiteralNode && right is UnitLiteralNode -> {
                BooleanLiteralNode(
                    range = node.range,
                    value = !negated
                )
            }
            isOptimizedLiteral(left) && isOptimizedLiteral(right) -> {
                // Literals of different types are considered not equal
                BooleanLiteralNode(
                    range = node.range,
                    value = negated
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

    private fun isOptimizedLiteral(node: Node): Boolean {
        return (node is IntLiteralNode ||
                node is BooleanLiteralNode ||
                node is StringLiteralNode ||
                node is UnitLiteralNode)
    }

    private fun comparison(node: BinaryExpressionNode, operator: BinaryOperator): Node {
        val left = apply(node.left) as ExpressionNode
        val right = apply(node.right) as ExpressionNode

        if(!constantFolding) return BinaryExpressionNode(
            range = node.range,
            operator = node.operator,
            left = left,
            right = right
        )

        return when {
            left is IntLiteralNode && right is IntLiteralNode -> {
                BooleanLiteralNode(
                    range = node.range,
                    value = compare(left.value, right.value, operator)
                )
            }
            left is StringLiteralNode && right is StringLiteralNode -> {
                BooleanLiteralNode(
                    range = node.range,
                    value = compare(left.value, right.value, operator)
                )
            }
            left is BooleanLiteralNode && right is BooleanLiteralNode -> {
                BooleanLiteralNode(
                    range = node.range,
                    value = compare(left.value, right.value, operator)
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

    private fun <T : Comparable<T>> compare(a: T, b: T, operator: BinaryOperator): Boolean {
        return when (operator) {
            GREATER_THAN -> (a > b)
            GREATER_THAN_OR_EQUAL_TO -> (a >= b)
            LESS_THAN -> (a < b)
            LESS_THAN_OR_EQUAL_TO -> (a <= b)
            else -> throw IllegalArgumentException("Invalid comparison operator provided")
        }
    }

    private fun logicalOr(node: BinaryExpressionNode): Node {
        val left = apply(node.left) as ExpressionNode
        val right = apply(node.right) as ExpressionNode

        return when {
            constantFolding && left is BooleanLiteralNode && right is BooleanLiteralNode -> {
                BooleanLiteralNode(
                    range = node.range,
                    value = left.value || right.value
                )
            }
            strengthReduction && (left is BooleanLiteralNode && left.value || right is BooleanLiteralNode && right.value) -> BooleanLiteralNode(
                range = node.range,
                value = true
            )
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

    private fun logicalAnd(node: BinaryExpressionNode): Node {
        val left = apply(node.left) as ExpressionNode
        val right = apply(node.right) as ExpressionNode

        return when {
            constantFolding && left is BooleanLiteralNode && right is BooleanLiteralNode -> {
                BooleanLiteralNode(
                    range = node.range,
                    value = left.value || right.value
                )
            }
            strengthReduction && (left is BooleanLiteralNode && !left.value || right is BooleanLiteralNode && !right.value) -> BooleanLiteralNode(
                range = node.range,
                value = false
            )
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

    private fun block(node: BlockNode): BlockNode = BlockNode(
        range = node.range,
        statements = node.statements.map { apply(it) as StatementNode }
    )

    private fun call(node: CallNode): CallNode = CallNode(
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

    private fun index(node: IndexNode): Node {
        val expr = apply(node.expression) as ExpressionNode
        val index = apply(node.index) as ExpressionNode

        return when {
            expr is ArrayLiteralNode && index is IntLiteralNode -> {
                // TODO: Update the range of the selected item
                expr.elements[index.value.toInt()]
            }
            else -> {
                IndexNode(
                    range = node.range,
                    expression = expr,
                    index = index
                )
            }
        }
    }

    private fun returnStmt(node: ReturnNode): ReturnNode = ReturnNode(
        range = node.range,
        expression = node.expression?.let { apply(it) } as ExpressionNode?
    )

    private fun unaryExpression(node: UnaryExpressionNode): Node {
        return when (node.operator) {
            NEGATION -> negation(node)
            LOGICAL_COMPLEMENT -> logicalComplement(node)
        }
    }

    private fun negation(node: UnaryExpressionNode): Node {
        return when (val inner = apply(node.operand) as ExpressionNode) {
            is IntLiteralNode -> IntLiteralNode(
                range = node.range,
                value = -inner.value
            )
            else -> UnaryExpressionNode(
                range = node.range,
                operator = node.operator,
                operand = inner
            )
        }
    }

    private fun logicalComplement(node: UnaryExpressionNode): Node {
        return when (val inner = apply(node.operand) as ExpressionNode) {
            is BooleanLiteralNode -> BooleanLiteralNode(
                range = node.range,
                value = !inner.value
            )
            else -> UnaryExpressionNode(
                range = node.range,
                operator = node.operator,
                operand = inner
            )
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