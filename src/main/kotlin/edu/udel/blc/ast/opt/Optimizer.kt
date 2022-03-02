package edu.udel.blc.ast.opt

import edu.udel.blc.ast.*
import edu.udel.blc.util.visitor.ValuedVisitor

class Optimizer : ValuedVisitor<Node, Node>() {

//    init {
//        register(FunctionDeclarationNode::class.java, ::functionDeclaration)
//        register(VariableDeclarationNode::class.java, ::variableDeclaration)
//
//        // statements
//        register(BlockNode::class.java, ::block)
//        register(ExpressionStatementNode::class.java, ::expressionStatement)
//        register(IfNode::class.java, ::`if`)
//        register(ReturnNode::class.java, ::`return`)
//        register(WhileNode::class.java, ::`while`)
//
//        register(ArrayLiteralNode::class.java, ::arrayLiteral)
//        register(BooleanLiteralNode::class.java, ::booleanLiteral)
//        register(IntLiteralNode::class.java, ::intLiteral)
//        register(StringLiteralNode::class.java, ::stringLiteral)
//        register(UnitLiteralNode::class.java, ::unitLiteral)
//
//        register(AssignmentNode::class.java, ::assignment)
//        register(BinaryExpressionNode::class.java, ::binaryExpression)
//        register(CallNode::class.java, ::call)
//        register(FieldSelectNode::class.java, ::fieldSelect)
//        register(IndexNode::class.java, ::index)
//        register(ReferenceNode::class.java, ::reference)
//        register(UnaryExpressionNode::class.java, ::unaryExpression)
//
//    }
//
//    fun functionDeclaration(decl: FunctionDeclarationNode): Node {
//        return decl.c
//

}