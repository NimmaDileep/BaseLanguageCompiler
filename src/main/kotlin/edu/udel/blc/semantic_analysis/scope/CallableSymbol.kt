package edu.udel.blc.semantic_analysis.scope

sealed class CallableSymbol(
    override val name: String,
    override val containingScope: Scope,
) : Symbol, Scope() {

    val parameters: List<VariableSymbol>
        get() = symbols
            .filterIsInstance<VariableSymbol>()
            .filter { it.isParameter }

}