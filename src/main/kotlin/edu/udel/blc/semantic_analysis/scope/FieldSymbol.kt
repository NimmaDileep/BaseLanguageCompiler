package edu.udel.blc.semantic_analysis.scope


class FieldSymbol(
    override val name: String,
    override val containingScope: Scope,
) : Symbol, MemberSymbol