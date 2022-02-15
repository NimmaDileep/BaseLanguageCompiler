package edu.udel.blc.machine_code.bytecode

import edu.udel.blc.semantic_analysis.type.*
import org.objectweb.asm.Type.*

object TypeUtils {

    val Type.descriptor: String
        get() = when (this) {
            AnyType -> "Ljava/lang/Object;"
            is ArrayType -> "Ljava/util/ArrayList;"
            BooleanType -> "Ljava/lang/Boolean;"
            is ClassType -> "L${name};"
            is FunctionType -> buildString {
                parameterTypes.values.joinTo(this, separator = "", prefix = "(", postfix = ")") { it.descriptor }
                append(returnType.descriptor)
            }
            IntType -> "Ljava/lang/Long;"
            StringType -> "Ljava/lang/String;"
            is StructType -> "L${name};"
            UnitType -> "Ljava/lang/Void;"
            else -> error("unreachable")
        }

    /**
     * Returns the [org.objectweb.asm.Type] for the runtime given [Type].
     */
    fun nativeType(type: Type): org.objectweb.asm.Type {
        return getType(type.descriptor)
    }

    fun methodDescriptor(returnType: org.objectweb.asm.Type, paramTypes: List<org.objectweb.asm.Type>): String =
        buildString {
            paramTypes.joinTo(this, separator = "", prefix = "(", postfix = ")") { it.descriptor }
            append(returnType.descriptor)
        }

    fun methodDescriptor(returnType: Type, paramTypes: List<Type>): String =
        methodDescriptor(nativeType(returnType), paramTypes.map { nativeType(it) })

    fun methodDescriptor(funType: FunctionType): String {
        return methodDescriptor(funType.returnType, funType.parameterTypes.values.toList())
    }
}