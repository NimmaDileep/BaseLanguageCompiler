package edu.udel.blc.semantic_analysis.scope

class GlobalScope(
    override val containingScope: Scope
) : Scope()