package edu.udel.blc.semantic_analysis.scope


class VariableSymbol(
    override val name: String,
    override val containingScope: Scope,
) : Symbol {

    override fun toString() = "Variable($name)"

    val isParameter: Boolean
        get() = containingScope is CallableSymbol



}

