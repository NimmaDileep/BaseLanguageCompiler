package edu.udel.blc.semantic_analysis.scope


sealed class Scope {

    abstract val containingScope: Scope?

    // Using a LinkedHashMap is important so that the order of parameters and fields is maintained
    val declarations = LinkedHashMap<String, Symbol>()

    val symbols: List<Symbol>
        get() = declarations.values.toList()

    open fun declare(symbol: Symbol): Symbol {
        declarations[symbol.name] = symbol
        return symbol
    }

    open fun lookup(name: String): Symbol? {
        return declarations[name] ?: containingScope?.lookup(name)
    }

    val enclosingPath: List<Scope> by lazy {
        buildList {
            var s: Scope? = this@Scope
            while (s != null) {
                add(s)
                s = s.containingScope
            }
        }
    }


}