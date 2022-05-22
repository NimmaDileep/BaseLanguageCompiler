package edu.udel.blc.machine_code.bytecode

import edu.udel.blc.ast.ClassDeclarationNode
import edu.udel.blc.ast.CompilationUnitNode
import edu.udel.blc.ast.FunctionDeclarationNode
import edu.udel.blc.ast.ReturnNode
import edu.udel.blc.machine_code.bytecode.TypeUtils.methodDescriptor
import edu.udel.blc.semantic_analysis.scope.ClassSymbol
import edu.udel.blc.semantic_analysis.type.ClassType
import edu.udel.blc.util.uranium.Reactor
import java.util.function.Function
import edu.udel.blc.machine_code.bytecode.TypeUtils.nativeType
import edu.udel.blc.semantic_analysis.scope.CallableSymbol
import edu.udel.blc.semantic_analysis.scope.FunctionSymbol
import edu.udel.blc.semantic_analysis.scope.MethodSymbol
import edu.udel.blc.semantic_analysis.type.FunctionType
import edu.udel.blc.semantic_analysis.type.UnitType
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Type
import org.objectweb.asm.Type.VOID_TYPE
import org.objectweb.asm.commons.Method

class ClassTranslator(
    private val reactor: Reactor,
    private val mainClazzType: Type
) : Function<CompilationUnitNode, List<ClassFileObject>> {
    override fun apply(compilationUnit: CompilationUnitNode): List<ClassFileObject> {
        val classNodes = compilationUnit.find<ClassDeclarationNode>()
        return topoSortClasses(classNodes).map { translate(it) }
    }

    /**
     * Sorts the class based on inheritance relationships so that superclasses are compiled before subclasses
     */
    private fun topoSortClasses(classes: List<ClassDeclarationNode>): List<ClassDeclarationNode> {
        val classSymbols = classes.associateBy { reactor.get<ClassSymbol>(it, "symbol") }
        val compileOrder = mutableListOf<ClassSymbol>()
        val visitedClasses = mutableSetOf<ClassSymbol>()

        fun visitClass(symbol: ClassSymbol) {
            if (visitedClasses.contains(symbol)) return
            visitedClasses.add(symbol)

            val superSymbol = symbol.superClassScope
            if (superSymbol != null) visitClass(superSymbol)

            compileOrder.add(symbol)
        }

        classSymbols.keys.forEach { visitClass(it) }

        require(compileOrder.size == classes.size) { "Not all classes are covered in the topological sort" }

        return compileOrder.map { classSymbols[it]!! }
    }

    private fun translate(node: ClassDeclarationNode): ClassFileObject {
        val classSymbol = reactor.get<ClassSymbol>(node, "symbol")
        val classType = reactor.get<ClassType>(classSymbol, "type")
        val superClassType = when (val superSymbol = classSymbol.superClassScope) {
            is ClassSymbol -> reactor.get<ClassType>(superSymbol, "type")
            else -> null
        }

        val superClassNativeType = superClassType?.let { nativeType(it) } ?: java_lang_Object

        val clazzType = nativeType(classType)

        return buildClass(
            access = ACC_PUBLIC,
            name = classType.name,
            superType = superClassNativeType
        ) { clazz ->
            classType.fieldTypes.entries.forEach { (name, type) ->
                clazz.visitField(
                    ACC_PUBLIC,
                    name,
                    nativeType(type)
                )
            }

            clazz.buildMethod(
                access = ACC_PUBLIC,
                name = "<init>",
                descriptor = methodDescriptor(
                    returnType = VOID_TYPE,
                    paramTypes = classType.fieldTypes.values.map { nativeType(it) }
                )
            ) { method ->
                method.loadThis()

                // invoke constructor of superclass
                when (superClassType) {
                    null -> method.invokeConstructor(java_lang_Object, "void <init>()")
                    else -> {
                        superClassType.fieldTypes.entries.forEachIndexed { index, _ ->
                            method.loadArg(index)
                        }

                        val descriptor = methodDescriptor(
                            VOID_TYPE,
                            superClassType.fieldTypes.map { nativeType(it.value) }
                        )
                        method.invokeConstructor(superClassNativeType, Method("<init>", descriptor))
                    }
                }

                classType.fieldTypes.entries.forEachIndexed { index, (name, type) ->
                    method.loadThis()
                    method.loadArg(index)
                    method.putField(clazzType, name, nativeType(type))
                }

                method.returnValue()
            }

            node.find<FunctionDeclarationNode>().forEach { methodNode ->
                // TODO: Add ImplicitArgumentGatherer to method translation

                val methodSymbol = reactor.get<MethodSymbol>(methodNode, "symbol")
                val overrides = reactor.get<MethodSymbol?>(methodSymbol, "overrides")
                val methodType = reactor.get<FunctionType>(methodSymbol, "type")
                val descriptor = methodDescriptor(methodType)

                methodSymbol.parameters.forEachIndexed { i, parameterSymbol ->
                    reactor[parameterSymbol, "index"] = i
                }

                val finalSymbol = overrides ?: methodSymbol

                clazz.buildMethod(
                    access = ACC_PUBLIC,
                    method = Method(finalSymbol.getQualifiedName("_"), descriptor)
                ) { method ->
                    val statementVisitor = StatementVisitor(clazzType, mainClazzType, clazz, method, reactor)
                    statementVisitor.accept(methodNode.body)

                    if (methodType.returnType == UnitType && methodNode.body.find<ReturnNode>().isEmpty()) {
                        method.push(null as String?)
                        method.returnValue()
                    }
                }
            }

        }
    }
}