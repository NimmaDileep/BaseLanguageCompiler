package edu.udel.blc.machine_code.bytecode

import edu.udel.blc.ast.*
import edu.udel.blc.ast.UnaryOperator.LOGICAL_COMPLEMENT
import edu.udel.blc.ast.UnaryOperator.NEGATION
import edu.udel.blc.machine_code.bytecode.TypeUtils.methodDescriptor
import edu.udel.blc.machine_code.bytecode.TypeUtils.nativeType
import edu.udel.blc.semantic_analysis.scope.FunctionSymbol
import edu.udel.blc.semantic_analysis.scope.StructSymbol
import edu.udel.blc.semantic_analysis.scope.Symbol
import edu.udel.blc.semantic_analysis.scope.VariableSymbol
import edu.udel.blc.semantic_analysis.type.ArrayType
import edu.udel.blc.semantic_analysis.type.FunctionType
import edu.udel.blc.semantic_analysis.type.StructType
import edu.udel.blc.semantic_analysis.type.Type
import edu.udel.blc.util.uranium.Reactor
import edu.udel.blc.util.visitor.Visitor
import org.objectweb.asm.Type.*
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.GeneratorAdapter.*
import org.objectweb.asm.commons.Method


class ExpressionVisitor(
    private val clazzType: org.objectweb.asm.Type,
    private val method: GeneratorAdapter,
    private val reactor: Reactor,
) : Visitor<ExpressionNode>() {

    init {
        register(ArrayLiteralNode::class.java, ::arrayLiteral)
        register(BooleanLiteralNode::class.java, ::booleanLiteral)
        register(IntLiteralNode::class.java, ::intLiteral)
        register(StringLiteralNode::class.java, ::stringLiteral)
        register(UnitLiteralNode::class.java, ::unitLiteral)

        register(AssignmentNode::class.java, ::assignment)
        register(BinaryExpressionNode::class.java, ::binaryExpression)
        register(CallNode::class.java, ::call)
        register(FieldSelectNode::class.java, ::fieldSelect)
        register(IndexNode::class.java, ::index)
        register(ReferenceNode::class.java, ::reference)
        register(UnaryExpressionNode::class.java, ::unaryExpression)
    }

    private fun booleanLiteral(node: BooleanLiteralNode) {
        method.push(node.value)
        method.box(BOOLEAN_TYPE)
    }

    private fun intLiteral(node: IntLiteralNode) {
        method.push(node.value)
        method.box(LONG_TYPE)
    }

    private fun stringLiteral(node: StringLiteralNode) {
        method.push(node.value)
    }

    private fun unitLiteral(node: UnitLiteralNode) {
        method.push(null as String?)
    }

    private fun arrayLiteral(node: ArrayLiteralNode) {
        method.newInstance(java_util_ArrayList)
        method.dup()
        method.push(node.elements.size)
        method.invokeConstructor(java_util_ArrayList, "void <init>(int)")
        node.elements.forEach { element ->
            method.dup()
            accept(element)
            method.invokeVirtual(java_util_ArrayList, "boolean add(Object)")
            method.pop()
        }
    }

    private fun assignment(node: AssignmentNode) {

        val type = reactor.get<Type>(node.expression, "type")
        val tmp = method.newLocal(nativeType(type))

        when (val lvalue = node.lvalue) {
            is ReferenceNode -> {
                accept(node.expression)
                method.dup()
                method.storeLocal(tmp)
                val symbol = reactor.get<Symbol>(lvalue, "symbol") as VariableSymbol
                val index = reactor.get<Int>(symbol, "index")
                when {
                    symbol.isParameter -> method.storeArg(index)
                    else -> method.storeLocal(index)
                }
            }
            is IndexNode -> {
                val arrayType = reactor.get<ArrayType>(lvalue.expression, "type")
                accept(lvalue.expression)
                accept(lvalue.index)
                method.unbox(LONG_TYPE)
                method.cast(LONG_TYPE, INT_TYPE)
                accept(node.expression)
                method.dup()
                method.storeLocal(tmp)
                method.arrayStore(nativeType(arrayType.elementType))
            }
            is FieldSelectNode -> {
                accept(lvalue.expression)
                accept(node.expression)
                method.dup()
                method.storeLocal(tmp)
                val structType = reactor.get<StructType>(lvalue.expression, "type")
                val fieldType = structType.fieldTypes[lvalue.name]!!
                method.putField(nativeType(structType), lvalue.name, nativeType(fieldType))
            }
            else -> TODO("Generate assignment to: $lvalue")
        }

        method.loadLocal(tmp)
    }

    private fun binaryExpression(node: BinaryExpressionNode) {
        when (node.operator) {
            BinaryOperator.ADDITION -> binaryMath(node, ADD)
            BinaryOperator.SUBTRACTION -> binaryMath(node, SUB)
            BinaryOperator.MULTIPLICATION -> binaryMath(node, MUL)
            BinaryOperator.REMAINDER -> binaryMath(node, REM)
            BinaryOperator.EQUAL_TO -> equality(node, EQ)
            BinaryOperator.NOT_EQUAL_TO -> equality(node, NE)
            BinaryOperator.GREATER_THAN -> comparison(node, GT)
            BinaryOperator.GREATER_THAN_OR_EQUAL_TO -> comparison(node, GE)
            BinaryOperator.LESS_THAN -> comparison(node, LT)
            BinaryOperator.LESS_THAN_OR_EQUAL_TO -> comparison(node, LE)
            BinaryOperator.LOGICAL_CONJUNCTION -> binaryLogic(node, EQ)
            BinaryOperator.LOGICAL_DISJUNCTION -> binaryLogic(node, NE)
        }
    }

    private fun binaryMath(node: BinaryExpressionNode, operator: Int) {
        accept(node.left)
        method.unbox(LONG_TYPE)
        accept(node.right)
        method.unbox(LONG_TYPE)
        method.math(operator, LONG_TYPE)
        method.box(LONG_TYPE)
    }

    private fun equality(node: BinaryExpressionNode, mode: Int) {
        accept(node.left)
        accept(node.right)
        method.invokeStatic(java_util_Objects, "boolean equals(Object, Object)")
        if (NE == mode) {
            val thenLabel = method.newLabel()
            val endLabel = method.newLabel()
            method.ifZCmp(mode, thenLabel)
            method.push(true)
            method.goTo(endLabel)
            method.mark(thenLabel)
            method.push(false)
            method.mark(endLabel)
        }
        method.box(BOOLEAN_TYPE)
    }

    private fun comparison(node: BinaryExpressionNode, mode: Int) {
        val thenLabel = method.newLabel()
        val endLabel = method.newLabel()
        accept(node.left)
        method.checkCast(java_lang_Comparable)
        accept(node.right)
        method.invokeInterface(java_lang_Comparable, "int compareTo(Object)")
        method.push(0)
        method.ifICmp(mode, thenLabel)
        method.push(false)
        method.goTo(endLabel)
        method.mark(thenLabel)
        method.push(true)
        method.mark(endLabel)
        method.box(BOOLEAN_TYPE)
    }

    private fun binaryLogic(node: BinaryExpressionNode, mode: Int) {
        val endLabel = method.newLabel()
        accept(node.left)
        method.dup()
        method.unbox(BOOLEAN_TYPE)
        method.ifZCmp(mode, endLabel)
        method.pop()
        accept(node.right)
        method.mark(endLabel)
    }

    private fun unaryExpression(node: UnaryExpressionNode) {
        when (node.operator) {
            LOGICAL_COMPLEMENT -> {
                val falseLabel = method.newLabel()
                val endLabel = method.newLabel()
                accept(node.operand)
                method.unbox(BOOLEAN_TYPE)
                method.ifZCmp(EQ, falseLabel)
                method.push(false)
                method.goTo(endLabel)
                method.mark(falseLabel)
                method.push(true)
                method.mark(endLabel)
                method.box(BOOLEAN_TYPE)
            }
            NEGATION -> unaryMath(node, NEG)
        }
    }

    private fun unaryMath(node: UnaryExpressionNode, operator: Int) {
        accept(node.operand)
        method.unbox(LONG_TYPE)
        method.math(operator, LONG_TYPE)
        method.box(LONG_TYPE)
    }

    private fun index(node: IndexNode) {
        val elementType = reactor.get<ArrayType>(node.expression, "type").elementType
        accept(node.expression)
        accept(node.index)
        method.unbox(LONG_TYPE)
        method.cast(LONG_TYPE, INT_TYPE)
        method.invokeVirtual(java_util_ArrayList, "Object get(int)")
        method.checkCast(nativeType(elementType))
    }

    private fun call(node: CallNode) {
        when (val callee = node.callee) {
            is ReferenceNode -> {
                when (val symbol = reactor.get<Symbol>(node.callee, "symbol")) {
                    is FunctionSymbol -> {
                        val functionType = reactor.get<FunctionType>(symbol, "type")
                        node.arguments.forEach { accept(it) }
                        method.invokeStatic(
                            clazzType,
                            Method(symbol.getQualifiedName("_"), methodDescriptor(functionType))
                        )
                    }
                    is StructSymbol -> {
                        val structType = reactor.get<StructType>(symbol, "type")
                        method.newInstance(nativeType(structType))
                        method.dup()
                        node.arguments.forEach { accept(it) }
                        val descriptor = methodDescriptor(
                            VOID_TYPE,
                            structType.fieldTypes.map { nativeType(it.value) }
                        )
                        method.invokeConstructor(nativeType(structType), Method("<init>", descriptor))
                    }
                    else -> TODO("generate call for $symbol")
                }
            }
            else -> TODO("generate call to $callee")
        }
    }

    private fun reference(node: ReferenceNode) {
        when (val symbol = reactor.get<Symbol>(node, "symbol")) {
            is VariableSymbol -> {
                val index = reactor.get<Int>(symbol, "index")
                when {
                    symbol.isParameter -> method.loadArg(index)
                    else -> method.loadLocal(index)
                }
            }
            else -> TODO("generate reference to $symbol")
        }
    }

    private fun fieldSelect(node: FieldSelectNode) {
        accept(node.expression)
        val structType = reactor.get<Type>(node.expression, "type")
        val fieldType = reactor.get<Type>(node, "type")
        method.getField(nativeType(structType), node.name, nativeType(fieldType))
    }

}
