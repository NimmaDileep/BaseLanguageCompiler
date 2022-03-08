package edu.udel.blc.machine_code.bytecode

import edu.udel.blc.ast.*
import edu.udel.blc.machine_code.bytecode.TypeUtils.methodDescriptor
import edu.udel.blc.machine_code.bytecode.TypeUtils.nativeType
import edu.udel.blc.semantic_analysis.scope.FunctionSymbol
import edu.udel.blc.semantic_analysis.scope.Symbol
import edu.udel.blc.semantic_analysis.type.FunctionType
import edu.udel.blc.semantic_analysis.type.Type
import edu.udel.blc.semantic_analysis.type.UnitType
import edu.udel.blc.util.uranium.Reactor
import edu.udel.blc.util.visitor.Visitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Type.BOOLEAN_TYPE
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.GeneratorAdapter.EQ
import org.objectweb.asm.commons.Method


class StatementVisitor(
    val clazzType: org.objectweb.asm.Type,
    val clazz: ClassWriter,
    val method: GeneratorAdapter,
    private val reactor: Reactor,
) : Visitor<StatementNode>() {

    private val expressionVisitor = ExpressionVisitor(clazzType, method, reactor)

    init {

        register(FunctionDeclarationNode::class.java, ::functionDeclaration)
        register(VariableDeclarationNode::class.java, ::variableDeclaration)

        // statements
        register(BlockNode::class.java, ::block)
        register(ExpressionStatementNode::class.java, ::expressionStatement)
        register(IfNode::class.java, ::`if`)
        register(ReturnNode::class.java, ::`return`)
        register(WhileNode::class.java, ::`while`)

    }

    fun functionDeclaration(node: FunctionDeclarationNode) {

        val implicitArguments = ImplicitArgumentGatherer.apply(reactor, node)
        if (implicitArguments.isNotEmpty()) {
            TODO("Handle closure")
        }

        val functionSymbol = reactor.get<FunctionSymbol>(node, "symbol")
        val functionType = reactor.get<FunctionType>(functionSymbol, "type")
        val descriptor = methodDescriptor(functionType)

        functionSymbol.parameters.forEachIndexed { i, parameterSymbol ->
            reactor[parameterSymbol, "index"] = i
        }

        clazz.buildMethod(
            access = ACC_PUBLIC or ACC_STATIC,
            method = Method(functionSymbol.getQualifiedName("_"), descriptor)
        ) { method ->
            val statementVisitor = StatementVisitor(clazzType, clazz, method, reactor)
            statementVisitor.accept(node.body)

            if(functionType.returnType == UnitType && node.body.find<ReturnNode>().isEmpty()) {
                method.push(null as String?)
                method.returnValue()
            }

        }
    }

    fun variableDeclaration(node: VariableDeclarationNode) {

        val symbol = reactor.get<Symbol>(node, "symbol")
        val type = reactor.get<Type>(symbol, "type")

        val local = method.newLocal(nativeType(type))
        reactor[symbol, "index"] = local

        expressionVisitor.accept(node.initializer)

        method.storeLocal(local)

    }

    fun block(node: BlockNode) {
        node.statements.forEach { accept(it) }
    }

    fun expressionStatement(node: ExpressionStatementNode) {
        expressionVisitor.accept(node.expression)
        // remove value from stack
        method.pop()
    }

    fun `return`(node: ReturnNode) {
        node.expression?.let { expressionVisitor.accept(it) }
        method.returnValue()
    }

    fun `if`(node: IfNode) {
        val elseLabel = method.newLabel()
        val endLabel = method.newLabel()

        expressionVisitor.accept(node.condition)
        method.unbox(BOOLEAN_TYPE)
        method.ifZCmp(EQ, elseLabel)
        accept(node.thenStatement)
        method.goTo(endLabel)
        method.mark(elseLabel)
        node.elseStatement?.let { accept(it) }
        method.mark(endLabel)
    }

    fun `while`(node: WhileNode) {
        val startLabel = method.newLabel()
        val endLabel = method.newLabel()

        method.mark(startLabel)
        expressionVisitor.accept(node.condition)
        method.unbox(BOOLEAN_TYPE)
        method.ifZCmp(EQ, endLabel)
        accept(node.body)
        method.goTo(startLabel)
        method.mark(endLabel)
    }


}