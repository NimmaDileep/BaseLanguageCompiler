package edu.udel.blc.machine_code.bytecode

import edu.udel.blc.ast.CompilationUnitNode
import edu.udel.blc.ast.StructDeclarationNode
import edu.udel.blc.machine_code.bytecode.TypeUtils.methodDescriptor
import edu.udel.blc.machine_code.bytecode.TypeUtils.nativeType
import edu.udel.blc.semantic_analysis.scope.StructSymbol
import edu.udel.blc.semantic_analysis.type.StructType
import edu.udel.blc.util.uranium.Reactor
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Type.VOID_TYPE
import org.objectweb.asm.commons.GeneratorAdapter.EQ
import org.objectweb.asm.commons.GeneratorAdapter.NE
import java.util.function.Function


class StructTranslator(
    private val reactor: Reactor,
) : Function<CompilationUnitNode, List<ClassFileObject>> {

    override fun apply(compilationUnit: CompilationUnitNode): List<ClassFileObject> =
        compilationUnit.find<StructDeclarationNode>().map { translate(it) }

    private fun translate(node: StructDeclarationNode): ClassFileObject {
        val structSymbol = reactor.get<StructSymbol>(node, "symbol")
        val structType = reactor.get<StructType>(structSymbol, "type")

        val clazzType = nativeType(structType)

        return buildClass(access = ACC_PUBLIC, name = structType.name) { clazz ->

            // declare fields
            structType.fieldTypes.entries.forEach { (name, type) ->
                clazz.visitField(
                    ACC_PUBLIC,
                    name,
                    nativeType(type)
                )
            }

            // create constructor
            clazz.buildMethod(
                access = ACC_PUBLIC,
                name = "<init>",
                descriptor = methodDescriptor(
                    returnType = VOID_TYPE,
                    paramTypes = structType.fieldTypes.values.map { nativeType(it) }
                )
            ) { method ->
                method.loadThis()
                method.invokeConstructor(java_lang_Object, "void <init>()")
                structType.fieldTypes.entries.forEachIndexed { index, (name, type) ->
                    method.loadThis()
                    method.loadArg(index)
                    method.putField(clazzType, name, nativeType(type))
                }
                method.returnValue()
            }

            // create toString
            clazz.buildMethod(
                access = ACC_PUBLIC,
                method = "String toString()",
            ) { method ->
                method.newInstance(java_lang_StringBuilder)
                method.dup()
                method.invokeConstructor(java_lang_StringBuilder, "void <init>()")

                method.push(node.name)
                method.invokeVirtual(java_lang_StringBuilder, "StringBuilder append(String)")
                method.push("{")
                method.invokeVirtual(java_lang_StringBuilder, "StringBuilder append(String)")
                structType.fieldTypes.entries.forEachIndexed { index, (name, type) ->
                    if (index >= 1) {
                        method.push(",")
                        method.invokeVirtual(java_lang_StringBuilder, "StringBuilder append(String)")
                    }
                    method.push(name)
                    method.invokeVirtual(java_lang_StringBuilder, "StringBuilder append(String)")
                    method.push("=")
                    method.invokeVirtual(java_lang_StringBuilder, "StringBuilder append(String)")
                    method.loadThis()
                    method.getField(clazzType, name, nativeType(type))
                    method.push("unit")
                    method.invokeStatic(java_util_Objects, "String toString(Object, String)")
                    method.invokeVirtual(java_lang_StringBuilder, "StringBuilder append(String)")
                }
                method.push("}")
                method.invokeVirtual(java_lang_StringBuilder, "StringBuilder append(String)")
                method.invokeVirtual(java_lang_StringBuilder, "String toString()")
                method.returnValue()
            }

            // create equals
            clazz.buildMethod(
                access = ACC_PUBLIC,
                method = "boolean equals(Object)"
            ) { method ->

                val elseLabel = method.newLabel()
                val falseLabel = method.newLabel()
                val endLabel = method.newLabel()

                // check equality
                method.loadThis()
                method.loadArg(0)
                // if not reference equal, check instance
                method.ifCmp(clazzType, NE, elseLabel)
                // if equal, return true
                method.push(true)
                method.goTo(endLabel)

                // check type
                method.mark(elseLabel)

                method.loadArg(0)
                method.instanceOf(clazzType)
                // if not same type, return false
                method.ifZCmp(EQ, falseLabel)

                val tmp = method.newLocal(clazzType)
                method.loadArg(0)
                method.checkCast(clazzType)
                method.storeLocal(tmp)

                // check fields
                structType.fieldTypes.entries.forEach { (name, type) ->
                    method.loadThis()
                    method.getField(clazzType, name, nativeType(type))

                    method.loadLocal(tmp)
                    method.getField(clazzType, name, nativeType(type))

                    // if field not equal, return false
                    method.invokeStatic(java_util_Objects, "boolean equals(Object, Object)")
                    method.ifZCmp(EQ, falseLabel)
                }
                // if all fields equal, return true
                method.push(true)
                method.goTo(endLabel)

                method.mark(falseLabel)
                method.push(false)

                method.mark(endLabel)
                method.returnValue()
            }
        }
    }
}
