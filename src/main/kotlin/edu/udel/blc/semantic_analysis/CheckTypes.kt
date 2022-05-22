package edu.udel.blc.semantic_analysis

import edu.udel.blc.ast.*
import edu.udel.blc.ast.UnaryOperator.LOGICAL_COMPLEMENT
import edu.udel.blc.ast.UnaryOperator.NEGATION
import edu.udel.blc.semantic_analysis.scope.CallableSymbol
import edu.udel.blc.semantic_analysis.scope.FunctionSymbol
import edu.udel.blc.semantic_analysis.scope.Symbol
import edu.udel.blc.semantic_analysis.type.*
import edu.udel.blc.util.uranium.Attribute
import edu.udel.blc.util.uranium.Reactor
import edu.udel.blc.util.visitor.ReflectiveAccessorWalker
import edu.udel.blc.util.visitor.WalkVisitType.PRE_VISIT
import java.lang.Integer.min
import java.util.function.Consumer


class CheckTypes(
    private val reactor: Reactor,
) : Consumer<CompilationUnitNode> {

    val walker = ReflectiveAccessorWalker(Node::class.java, PRE_VISIT).apply {

        // declarations
        register(VariableDeclarationNode::class.java, PRE_VISIT, ::variableDeclaration)

        // expressions
        register(IndexNode::class.java, PRE_VISIT, ::index)
        register(BinaryExpressionNode::class.java, PRE_VISIT, ::binaryExpression)
        register(CallNode::class.java, PRE_VISIT, ::call)
        register(UnaryExpressionNode::class.java, PRE_VISIT, ::unaryExpression)

        // statements
        register(AssignmentNode::class.java, PRE_VISIT, ::assignment)
        register(IfNode::class.java, PRE_VISIT, ::ifStmt)
        register(WhileNode::class.java, PRE_VISIT, ::whileStmt)
        register(ReturnNode::class.java, PRE_VISIT, ::returnStmt)
    }

    override fun accept(compilationUnit: CompilationUnitNode) {
        walker.accept(compilationUnit)
        reactor.run()
    }

    private fun index(node: IndexNode) {
        checkType("check index is Int", node.index, IntType)
    }

    private fun call(node: CallNode) {

        val calleeTypeAttribute = Attribute(node.callee, "type")
        val argumentTypeAttributes = node.arguments.map { Attribute(it, "type") }

        reactor.rule("check whether argument types match parameter types") {
            using(calleeTypeAttribute)
            using(argumentTypeAttributes)
            by { r ->
                val parameterTypes = when (val type = r.get<Type>(calleeTypeAttribute)) {
                    is FunctionType -> type.parameterTypes.entries.toList()
                    is StructType -> type.fieldTypes.entries.toList()
                    else -> return@by
                }
                val argumentTypes = argumentTypeAttributes.map { r.get<Type>(it) }

                if (parameterTypes.size != argumentTypes.size) {
                    reactor.error(
                        SemanticError(
                            node,
                            "expected ${parameterTypes.size} ${
                                when (parameterTypes.size) {
                                    1 -> "argument"
                                    else -> "arguments"
                                }
                            } but got ${argumentTypes.size}"
                        )
                    )
                }

                (0 until min(parameterTypes.size, argumentTypes.size)).forEach { index ->
                    val (parameterName, parameterType) = parameterTypes[index]
                    val argument = node.arguments[index]
                    val argumentType = argumentTypes[index]
                    if (!argumentType.isAssignableTo(parameterType)) {
                        reactor.error(
                            SemanticError(
                                argument,
                                "incompatible argument: expected $parameterType but got $argumentType"
                            )
                        )
                    }
                }
            }
        }
    }


    private fun binaryExpression(node: BinaryExpressionNode) {
        when (node.operator) {
            BinaryOperator.ADDITION,
            BinaryOperator.SUBTRACTION,
            BinaryOperator.MULTIPLICATION,
            BinaryOperator.REMAINDER -> binaryMath(node)

            BinaryOperator.EQUAL_TO,
            BinaryOperator.NOT_EQUAL_TO -> equality(node)

            BinaryOperator.GREATER_THAN,
            BinaryOperator.GREATER_THAN_OR_EQUAL_TO,
            BinaryOperator.LESS_THAN,
            BinaryOperator.LESS_THAN_OR_EQUAL_TO -> comparison(node)

            BinaryOperator.LOGICAL_CONJUNCTION,
            BinaryOperator.LOGICAL_DISJUNCTION -> binaryLogic(node)
        }
    }


    private fun binaryMath(node: BinaryExpressionNode) {
        checkType("check left operand for binary math", node.left, IntType)
        checkType("check right operand for binary math", node.right, IntType)
    }

    private fun equality(node: BinaryExpressionNode) {

    }

    private fun comparison(node: BinaryExpressionNode) {
        checkType("check left operand for comparison", node.left, BooleanType, IntType, StringType)
        checkType("check right operand for comparison", node.right, BooleanType, IntType, StringType)

        val leftTypeAttribute = Attribute(node.left, "type")
        val rightTypeAttribute = Attribute(node.right, "type")

        reactor.rule("check left and right operands are the same type") {
            using(leftTypeAttribute, rightTypeAttribute)
            by { r ->
                val leftType = r.get<Type>(leftTypeAttribute)
                val rightType = r.get<Type>(rightTypeAttribute)

                if (leftType != rightType) {
                    reactor.error(SemanticError(node, "can not compare $leftType to $rightType"))
                }
            }
        }
    }

    private fun binaryLogic(node: BinaryExpressionNode) {
        checkType("check left operand for binary logic", node.left, BooleanType)
        checkType("check right operand for binary logic", node.right, BooleanType)
    }

    private fun unaryExpression(node: UnaryExpressionNode) {
        when (node.operator) {
            LOGICAL_COMPLEMENT -> checkType("check operand for logical complement", node.operand, BooleanType)
            NEGATION -> checkType("check operand for negation", node.operand, IntType)
        }
    }

    private fun checkAssignable(name: String = "", from: ExpressionNode, to: Any) {

        val variableTypeAttribute = Attribute(to, "type")
        val expressionTypeAttribute = Attribute(from, "type")

        reactor.rule(name) {
            using(variableTypeAttribute, expressionTypeAttribute)
            by { r ->
                val variableType = r.get<Type>(variableTypeAttribute)
                val expressionType = r.get<Type>(expressionTypeAttribute)

                if (!expressionType.isAssignableTo(variableType)) {
                    reactor.error(
                        SemanticError(from, "invalid type $expressionType, expected $variableType")
                    )
                }
            }
        }
    }


    private fun assignment(node: AssignmentNode) {
        checkAssignable(
            name = "check whether expression type matches variable type",
            from = node.expression,
            to = node.lvalue,
        )
    }

    private fun variableDeclaration(node: VariableDeclarationNode) {
        reactor.on(
            name = "load variable declaration symbol",
            attribute = Attribute(node, "symbol")
        ) { symbol: Symbol ->
            checkAssignable(
                name = "check whether initializer type matches variable type",
                from = node.initializer,
                to = symbol,
            )
        }
    }

    private fun ifStmt(node: IfNode) {
        checkType("check condition for if", node.condition, BooleanType)
    }

    private fun whileStmt(node: WhileNode) {
        checkType("check condition for while", node.condition, BooleanType)
    }

    private fun returnStmt(node: ReturnNode) {
        reactor.on(
            name = "check return",
            attribute = Attribute(node, "containingFunction")
        ) { containingFunction: CallableSymbol ->

            reactor.on(
                name = "check return type",
                attribute = Attribute(containingFunction, "type")
            ) { functionType: FunctionType ->

                val returnType = functionType.returnType

                when (val expression = node.expression) {
                    null -> if (returnType != UnitType) {
                        reactor.error(
                            SemanticError(node, "return without value in a function with non-Unit return type.")
                        )
                    }
                    else -> {
                        reactor.on(
                            name = "check return value",
                            Attribute(expression, "type"),
                        ) { expressionType: Type ->

                            if (!expressionType.isAssignableTo(returnType)) {
                                reactor.error(
                                    SemanticError(
                                        expression,
                                        "incompatible return value, expected $returnType but got $expressionType"
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkType(name: String = "", node: ExpressionNode, vararg types: Type) {
        val attribute = Attribute(node, "type")

        reactor.rule(name) {
            using(attribute)
            by { r ->
                val value = r.get<Type>(attribute)
                if (value !in types) {
                    reactor.error(SemanticError(node, "invalid type $value, expecting one of ${types.joinToString { it.toString() }}"))
                }
            }
        }
    }

}
