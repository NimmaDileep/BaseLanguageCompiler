package edu.udel.blc.semantic_analysis.type

data class ArrayType(
    val elementType: Type,
) : Type {

    override fun toString(): String = "[$elementType]"

}