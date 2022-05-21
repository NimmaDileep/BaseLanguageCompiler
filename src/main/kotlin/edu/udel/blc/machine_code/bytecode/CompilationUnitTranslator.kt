package edu.udel.blc.machine_code.bytecode

import edu.udel.blc.ast.CompilationUnitNode
import edu.udel.blc.util.uranium.Reactor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Type.*
import java.util.function.Function


class CompilationUnitTranslator(
    private val compilationUnitName: String,
    private val reactor: Reactor,
) : Function<CompilationUnitNode, Bytecode> {

    private val clazzType = getType("L$compilationUnitName;")

    override fun apply(node: CompilationUnitNode): Bytecode {

        val structs = StructTranslator(reactor).apply(node)
        val classes = ClassTranslator(reactor).apply(node)
        val addedClasses = structs + classes

        val mainClass = buildClass(
            access = ACC_PUBLIC,
            name = compilationUnitName
        ) { clazz ->

            generateBuiltins(clazz)

            // main
            clazz.buildMethod(
                access = ACC_PUBLIC or ACC_STATIC,
                method = "void main(String[])",
            ) { method ->
                val visitor = StatementVisitor(clazzType, clazz, method, reactor)
                node.statements.forEach { visitor.accept(it) }
                method.returnValue()
            }

        }

        return Bytecode(mainClass, addedClasses)
    }

    private fun generateBuiltins(clazz: ClassWriter) {

        clazz.buildMethod(
            access = ACC_PUBLIC or ACC_STATIC,
            method = "Long len(java.util.ArrayList)"
        ) { method ->
            method.loadArg(0)
            method.invokeVirtual(java_util_ArrayList, "int size()")
            method.cast(INT_TYPE, LONG_TYPE)
            method.box(LONG_TYPE)
            method.returnValue()
        }

        clazz.buildMethod(
            access = ACC_PUBLIC or ACC_STATIC,
            method = "String str(Object)"
        ) { method ->
            method.loadArg(0)
            method.push("unit")
            method.invokeStatic(java_util_Objects, "String toString(Object, String)")
            method.returnValue()
        }

        clazz.buildMethod(
            access = ACC_PUBLIC or ACC_STATIC,
            method = "Void print(Object)"
        ) { method ->
            method.getStatic(java_lang_System, "out", java_io_PrintStream)
            method.loadArg(0)
            method.invokeStatic(clazzType, "String str(Object)")
            method.invokeVirtual(java_io_PrintStream, "void println(String)")
            method.push(null as String?)
            method.returnValue()
        }

        clazz.buildMethod(
            access = ACC_PUBLIC or ACC_STATIC,
            method = "String concat(String, String)"
        ) { method ->
            method.newInstance(java_lang_StringBuilder)
            method.dup()
            method.invokeConstructor(java_lang_StringBuilder, "void <init>()")
            method.loadArg(0)
            method.invokeVirtual(java_lang_StringBuilder, "StringBuilder append(String)")
            method.loadArg(1)
            method.invokeVirtual(java_lang_StringBuilder, "StringBuilder append(String)")
            method.invokeVirtual(java_lang_StringBuilder, "String toString()")
            method.returnValue()
        }
    }

}