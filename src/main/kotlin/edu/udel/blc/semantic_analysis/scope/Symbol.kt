package edu.udel.blc.semantic_analysis.scope


sealed interface Symbol {

    val name: String
    val containingScope: Scope

    val qualifiedName: String
        get() = getQualifiedName()

    fun getQualifiedName(separator: String = "_") = buildString {
        val names = buildList {
            add(name)
            containingScope.enclosingPath.mapNotNullTo(this) { scope ->
                when (scope) {
                    is ClassSymbol -> scope.name
                    is FunctionSymbol -> scope.name
                    is MethodSymbol -> scope.name
                    else -> null
                }
            }
        }
        names.reversed().joinTo(this, separator = separator)
    }

}