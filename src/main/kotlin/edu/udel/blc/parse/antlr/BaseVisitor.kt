package edu.udel.blc.parse.antlr

import BaseBaseVisitor
import BaseLexer
import BaseParser
import edu.udel.blc.ast.*
import edu.udel.blc.ast.BinaryOperator.*
import edu.udel.blc.ast.UnaryOperator.LOGICAL_COMPLEMENT
import edu.udel.blc.ast.UnaryOperator.NEGATION
import org.antlr.v4.runtime.ParserRuleContext

class BaseVisitor : BaseBaseVisitor<Node>() {

    val ParserRuleContext.range: IntRange
        get() {
            return start.startIndex..stop.stopIndex
        }

    override fun visitCompilatonUnit(ctx: BaseParser.CompilatonUnitContext): CompilationUnitNode {
        val statements = ctx.declarations.map { it.accept(this) as StatementNode }
        return CompilationUnitNode(ctx.range, statements)
    }

    override fun visitFunctionDeclaration(
            ctx: BaseParser.FunctionDeclarationContext
    ): FunctionDeclarationNode {
        val name = ctx.name.text
        val params = ctx.parameters.map { it.accept(this) as ParameterNode }
        val returnType = ctx.returnType.accept(this) as Node
        val body = ctx.body.accept(this) as BlockNode
        return FunctionDeclarationNode(ctx.range, name, params, returnType, body)
    }

    override fun visitParameter(ctx: BaseParser.ParameterContext): ParameterNode {
        val name = ctx.name.text
        val type = ctx.type.accept(this) as Node
        return ParameterNode(ctx.range, name, type)
    }

    override fun visitStructDeclaration(
            ctx: BaseParser.StructDeclarationContext
    ): StructDeclarationNode {
        val name = ctx.name.text
        val fields = ctx.fields.map { it.accept(this) as FieldNode }
        return StructDeclarationNode(ctx.range, name, fields)
    }

    override fun visitField(ctx: BaseParser.FieldContext): FieldNode {
        val name = ctx.name.text
        val type = ctx.type.accept(this) as Node
        return FieldNode(ctx.range, name, type)
    }

    override fun visitVariable(ctx: BaseParser.VariableContext): FieldNode {
        val name = ctx.name.text
        val type = ctx.type.accept(this) as Node
        return FieldNode(ctx.range, name, type)
    }

    override fun visitClassDeclaration(
            ctx: BaseParser.ClassDeclarationContext
    ): ClassDeclarationNode {
        val name = ctx.name.text
        val members = ctx.members.map { it.accept(this) as Node }

        val fields = members.filterIsInstance<FieldNode>()
        val methods = members.filterIsInstance<FunctionDeclarationNode>()

        return ClassDeclarationNode(ctx.range, name, fields, methods)
    }

    override fun visitFieldMember(ctx: BaseParser.FieldMemberContext): FieldNode {
        val name = ctx.variable().name.text
        val type = ctx.variable().type.accept(this) as Node

        return FieldNode(ctx.range, name, type)
    }

    override fun visitVariableDeclaration(
            ctx: BaseParser.VariableDeclarationContext
    ): VariableDeclarationNode {
        val name = ctx.variable().name.text
        val type = ctx.variable().type.accept(this) as Node
        val initializer = ctx.initializer.accept(this) as ExpressionNode
        return VariableDeclarationNode(ctx.range, name, type, initializer)
    }

    // Stmt

    override fun visitBlock(ctx: BaseParser.BlockContext): BlockNode {
        val declarations = ctx.declarations.map { it.accept(this) as StatementNode }
        return BlockNode(ctx.range, declarations)
    }

    override fun visitExpressionStmt(
            ctx: BaseParser.ExpressionStmtContext
    ): ExpressionStatementNode {
        val expr = ctx.expression.accept(this) as ExpressionNode
        return ExpressionStatementNode(ctx.range, expr)
    }

    override fun visitIfStmt(ctx: BaseParser.IfStmtContext): IfNode {
        val condition = ctx.condition.accept(this) as ExpressionNode
        val thenStmt = ctx.thenStatement.accept(this) as StatementNode
        val elseStmt = ctx.elseStatement?.accept(this) as? StatementNode
        return IfNode(ctx.range, condition, thenStmt, elseStmt)
    }

    override fun visitReturnStmt(ctx: BaseParser.ReturnStmtContext): ReturnNode {
        val expression = ctx.expression?.accept(this) as? ExpressionNode
        return ReturnNode(ctx.range, expression)
    }

    override fun visitWhileStmt(ctx: BaseParser.WhileStmtContext): WhileNode {
        val condition = ctx.condition.accept(this) as ExpressionNode
        val body = ctx.body.accept(this) as StatementNode
        return WhileNode(ctx.range, condition, body)
    }

    // expr

    override fun visitAssignment(ctx: BaseParser.AssignmentContext): AssignmentNode {
        val lvalue = ctx.lvalue.accept(this) as ExpressionNode
        val expression = ctx.expression.accept(this) as ExpressionNode
        return AssignmentNode(ctx.range, lvalue, expression)
    }

    override fun visitBinary(ctx: BaseParser.BinaryContext): BinaryExpressionNode {
        val left = ctx.left.accept(this) as ExpressionNode
        val right = ctx.right.accept(this) as ExpressionNode
        val operator =
                when (ctx.operator.type) {
                    BaseLexer.CONJ -> LOGICAL_CONJUNCTION
                    BaseLexer.DISJ -> LOGICAL_DISJUNCTION
                    BaseLexer.EQUAL_EQUAL -> EQUAL_TO
                    BaseLexer.BANG_EQUAL -> NOT_EQUAL_TO
                    BaseLexer.LESS -> LESS_THAN
                    BaseLexer.LESS_EQUAL -> LESS_THAN_OR_EQUAL_TO
                    BaseLexer.GREATER -> GREATER_THAN
                    BaseLexer.GREATER_EQUAL -> GREATER_THAN_OR_EQUAL_TO
                    BaseLexer.PERCENT -> REMAINDER
                    BaseLexer.STAR -> MULTIPLICATION
                    BaseLexer.PLUS -> ADDITION
                    BaseLexer.MINUS -> SUBTRACTION
                    BaseLexer.SLASH -> DIVISION
                    else -> error("Unknown binary operator: ${ctx.operator.text}")
                }
        return BinaryExpressionNode(ctx.range, operator, left, right)
    }

    override fun visitUnaryPrefix(ctx: BaseParser.UnaryPrefixContext): UnaryExpressionNode {
        val operand = ctx.operand.accept(this) as ExpressionNode
        val operator =
                when (ctx.operator.type) {
                    BaseLexer.BANG -> LOGICAL_COMPLEMENT
                    BaseLexer.MINUS -> NEGATION
                    else -> error("Unknown unary operator: ${ctx.operator.text}")
                }
        return UnaryExpressionNode(ctx.range, operator, operand)
    }

    override fun visitFunctionCall(ctx: BaseParser.FunctionCallContext): CallNode {
        val callee = ReferenceNode(ctx.range, ctx.callee.text)
        val arguments = ctx.arguments.map { it.accept(this) as ExpressionNode }
        return CallNode(ctx.range, callee, arguments)
    }

    override fun visitMethodCall(ctx: BaseParser.MethodCallContext): MethodCallNode {
        val callee = ctx.callee.text
        val arguments = ctx.arguments.map { it.accept(this) as ExpressionNode }
        val receiver = ctx.receiver.accept(this) as ExpressionNode

        return MethodCallNode(ctx.range, callee, arguments, receiver)
    }

    override fun visitIndex(ctx: BaseParser.IndexContext): IndexNode {
        val expression = ctx.expression.accept(this) as ExpressionNode
        val index = ctx.index.accept(this) as ExpressionNode
        return IndexNode(ctx.range, expression, index)
    }

    override fun visitFieldSelect(ctx: BaseParser.FieldSelectContext): FieldSelectNode {
        val expression = ctx.expression.accept(this) as ExpressionNode
        val name = ctx.name.text
        return FieldSelectNode(ctx.range, expression, name)
    }

    // literal

    override fun visitArrayLiteral(ctx: BaseParser.ArrayLiteralContext): ArrayLiteralNode {
        val elements = ctx.elements.map { it.accept(this) as ExpressionNode }
        return ArrayLiteralNode(ctx.range, elements)
    }

    override fun visitBooleanLiteral(ctx: BaseParser.BooleanLiteralContext): BooleanLiteralNode {
        return BooleanLiteralNode(ctx.range, ctx.value.text.toBoolean())
    }

    override fun visitIntLiteral(ctx: BaseParser.IntLiteralContext): IntLiteralNode {
        return IntLiteralNode(ctx.range, ctx.value.text.toLong())
    }

    override fun visitStringLiteral(ctx: BaseParser.StringLiteralContext): StringLiteralNode {
        val text = ctx.value.text.substring(1, ctx.value.text.length - 1)
        return StringLiteralNode(ctx.range, text)
    }

    override fun visitUnitLiteral(ctx: BaseParser.UnitLiteralContext): UnitLiteralNode {
        return UnitLiteralNode(ctx.range)
    }

    override fun visitSelf(ctx: BaseParser.SelfContext): SelfNode {
        return SelfNode(ctx.range)
    }

    // type

    override fun visitArrayType(ctx: BaseParser.ArrayTypeContext): ArrayTypeNode {
        val elementType = ctx.elementType.accept(this) as Node
        return ArrayTypeNode(ctx.range, elementType)
    }

    //

    override fun visitReference(ctx: BaseParser.ReferenceContext): ReferenceNode {
        val name = ctx.name.text
        return ReferenceNode(ctx.range, name)
    }
}
