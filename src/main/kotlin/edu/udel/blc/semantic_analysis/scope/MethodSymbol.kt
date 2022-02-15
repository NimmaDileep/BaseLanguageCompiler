package edu.udel.blc.semantic_analysis.scope


class MethodSymbol(
    name: String,
    containingScope: Scope,
) : CallableSymbol(name, containingScope), MemberSymbol {

    override fun toString() = "Method($name)"

}
