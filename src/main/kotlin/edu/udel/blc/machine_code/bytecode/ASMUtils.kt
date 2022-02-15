package edu.udel.blc.machine_code.bytecode

import edu.udel.blc.semantic_analysis.type.*
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.GeneratorAdapter
import org.objectweb.asm.commons.Method
import org.objectweb.asm.commons.Method.getMethod
import java.io.PrintStream
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

val java_lang_Boolean: org.objectweb.asm.Type = org.objectweb.asm.Type.getType(Boolean::class.java)!!
val java_lang_Integer: org.objectweb.asm.Type = org.objectweb.asm.Type.getType(Integer::class.java)!!
val java_lang_Void: org.objectweb.asm.Type = org.objectweb.asm.Type.getType(Void::class.java)!!
val java_lang_Class: org.objectweb.asm.Type = org.objectweb.asm.Type.getType(Class::class.java)!!
val java_lang_Long: org.objectweb.asm.Type = org.objectweb.asm.Type.getType(Long::class.java)!!
val java_lang_Object: org.objectweb.asm.Type = org.objectweb.asm.Type.getType(Object::class.java)!!
val java_lang_System: org.objectweb.asm.Type = org.objectweb.asm.Type.getType(System::class.java)!!
val java_util_Objects: org.objectweb.asm.Type = org.objectweb.asm.Type.getType(Objects::class.java)!!
val java_util_Arrays: org.objectweb.asm.Type = org.objectweb.asm.Type.getType(Arrays::class.java)!!
val java_util_Comparator: org.objectweb.asm.Type = org.objectweb.asm.Type.getType(Comparator::class.java)!!
val java_util_ArrayList: org.objectweb.asm.Type = org.objectweb.asm.Type.getType(ArrayList::class.java)!!

val java_lang_String: org.objectweb.asm.Type = org.objectweb.asm.Type.getType(String::class.java)!!
val java_io_PrintStream: org.objectweb.asm.Type = org.objectweb.asm.Type.getType(PrintStream::class.java)!!
val java_lang_StringBuilder: org.objectweb.asm.Type = org.objectweb.asm.Type.getType(StringBuilder::class.java)!!
val java_lang_Comparable: org.objectweb.asm.Type = org.objectweb.asm.Type.getType(Comparable::class.java)!!

fun buildClass(
    flags: Int = ClassWriter.COMPUTE_FRAMES,
    version: Int = Opcodes.V16,
    access: Int,
    name: String,
    signature: String? = null,
    superType: org.objectweb.asm.Type = java_lang_Object,
    interfaces: List<org.objectweb.asm.Type> = emptyList(),
    builderAction: (ClassWriter) -> Unit
): ClassFileObject {
    val writer = ClassWriter(flags)
    writer.visit(
        version,
        access,
        name,
        signature,
        superType.internalName,
        interfaces.map { it.internalName }.toTypedArray()
    )
    builderAction(writer)
    writer.visitEnd()
    return ClassFileObject(name, writer.toByteArray())
}



fun ClassWriter.buildMethod(
    access: Int,
    method: String,
    signature: String? = null,
    exceptions: List<org.objectweb.asm.Type> = emptyList(),
    builderAction: (GeneratorAdapter) -> Unit
) {
    buildMethod(access, getMethod(method), signature, exceptions, builderAction)
}

fun ClassWriter.buildMethod(
    access: Int,
    name: String,
    descriptor: String,
    signature: String? = null,
    exceptions: List<org.objectweb.asm.Type> = emptyList(),
    builderAction: (GeneratorAdapter) -> Unit
) {
    return buildMethod(access, Method(name, descriptor), signature, exceptions, builderAction)
}

fun ClassWriter.buildMethod(
    access: Int,
    method: Method,
    signature: String? = null,
    exceptions: List<org.objectweb.asm.Type> = emptyList(),
    builderAction: (GeneratorAdapter) -> Unit
) {
    val adapter = GeneratorAdapter(access, method, signature, exceptions.toTypedArray(), this)
    builderAction(adapter)
    adapter.endMethod()
}


fun ClassWriter.visitField(
    access: Int,
    name: String,
    type: org.objectweb.asm.Type,
    signature: String? = null,
    value: Any? = null
) {
    visitField(access, name, type.descriptor, signature, value)
}

fun GeneratorAdapter.invokeVirtual(
    owner: org.objectweb.asm.Type,
    method: String,
) = invokeVirtual(owner, getMethod(method))


fun GeneratorAdapter.invokeStatic(
    owner: org.objectweb.asm.Type,
    method: String,
) = invokeStatic(owner, getMethod(method))

fun GeneratorAdapter.invokeConstructor(
    owner: org.objectweb.asm.Type,
    method: String,
) = invokeConstructor(owner, getMethod(method))

fun GeneratorAdapter.invokeInterface(
    owner: org.objectweb.asm.Type,
    method: String,
) = invokeInterface(owner, getMethod(method))

