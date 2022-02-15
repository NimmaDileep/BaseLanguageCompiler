package edu.udel.blc.semantic_analysis.scope


class PrimitiveTypeSymbol(
    override val name: String,
    override val containingScope: Scope,
) : TypeSymbol {

    override fun toString(): String = "Primitive($name)"

}