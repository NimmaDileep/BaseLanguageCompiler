package edu.udel.blc.semantic_analysis.type

sealed interface Type {

    /**
     * returns true if other is the same as or a supertype of this
     */
    infix fun isAssignableTo(other: Type): Boolean {
        val result = when {
            this == other -> true
            other is AnyType -> true
            this is ArrayType && other is ArrayType -> this.elementType isAssignableTo  other.elementType
            else -> false
        }
        return result
    }

    fun commonSupertype(other: Type): Type {
        return when {
            this.isAssignableTo(other) -> other
            other.isAssignableTo(this) -> this
            else -> AnyType
        }
    }

}