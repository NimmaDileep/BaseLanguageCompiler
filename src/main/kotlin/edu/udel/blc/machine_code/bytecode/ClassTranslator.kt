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
import edu.udel.blc.semantic_analysis.scope.FunctionSymbol
import edu.udel.blc.semantic_analysis.scope.MethodSymbol
import edu.udel.blc.semantic_analysis.type.FunctionType
import edu.udel.blc.semantic_analysis.type.UnitType
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Type.VOID_TYPE
import org.objectweb.asm.commons.Method

class ClassTranslator(
    private val reactor: Reactor
) : Function<CompilationUnitNode, List<ClassFileObject>> {
    override fun apply(compilationUnit: CompilationUnitNode): List<ClassFileObject> =
        compilationUnit.find<ClassDeclarationNode>().map { translate(it) }

    private fun translate(node: ClassDeclarationNode): ClassFileObject {
        val classSymbol = reactor.get<ClassSymbol>(node, "symbol")
        val classType = reactor.get<ClassType>(classSymbol, "type")

        val clazzType = nativeType(classType)

        return buildClass(access = ACC_PUBLIC, name = classType.name) { clazz ->

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
                method.invokeConstructor(java_lang_Object, "void <init>()")
                classType.fieldTypes.entries.forEachIndexed { index, (name, type) ->
                    method.loadThis()
                    method.loadArg(index)
                    method.putField(clazzType, name, nativeType(type))
                }

                method.returnValue()
            }

            node.find<FunctionDeclarationNode>().forEach {
                methodNode ->
                // TODO: Add ImplicitArgumentGatherer to method translation

                val methodSymbol = reactor.get<MethodSymbol>(methodNode, "symbol")
                val methodType = reactor.get<FunctionType>(methodSymbol, "type")
                val descriptor = methodDescriptor(methodType)

                methodSymbol.parameters.forEachIndexed { i, parameterSymbol ->
                    reactor[parameterSymbol, "index"] = i
                }

                clazz.buildMethod(
                    access = ACC_PUBLIC,
                    method = Method(methodSymbol.getQualifiedName("_"), descriptor)
                ) {
                    method ->
                    val statementVisitor = StatementVisitor(clazzType, clazz, method, reactor)
                    statementVisitor.accept(methodNode.body)

                    if(methodType.returnType == UnitType && methodNode.body.find<ReturnNode>().isEmpty()) {
                        method.push(null as String?)
                        method.returnValue()
                    }
                }
            }

        }
    }
}