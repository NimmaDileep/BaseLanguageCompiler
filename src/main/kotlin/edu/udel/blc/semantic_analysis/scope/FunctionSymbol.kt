package edu.udel.blc.semantic_analysis.scope


class FunctionSymbol(
    name: String,
    containingScope: Scope,
) : CallableSymbol(name, containingScope) {

    override fun toString() = "Function($name)"

}