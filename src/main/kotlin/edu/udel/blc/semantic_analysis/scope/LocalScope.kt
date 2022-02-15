package edu.udel.blc.semantic_analysis.scope

class LocalScope(
    override val containingScope: Scope
) : Scope()