package edu.udel.blc.semantic_analysis.scope


class StructSymbol(
    override val name: String,
    override val containingScope: Scope,
) : TypeSymbol, Scope() {

    val fields: List<FieldSymbol>
        get() = symbols.filterIsInstance<FieldSymbol>()

}