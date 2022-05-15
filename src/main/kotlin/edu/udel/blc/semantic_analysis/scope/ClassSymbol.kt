package edu.udel.blc.semantic_analysis.scope


class ClassSymbol(
    override val name: String,
    val superClassName: String? = null,
    override val containingScope: Scope,
) : TypeSymbol, Scope() {

    val superClassScope: ClassSymbol?
        get() = when (superClassName) {
            null -> null
            else -> containingScope.lookup(superClassName) as? ClassSymbol
        }

    override fun declare(symbol: Symbol): MemberSymbol {
        require(symbol is MemberSymbol) { "$symbol is not a MemberSymbol" }
        return super.declare(symbol) as MemberSymbol
    }

    fun resolveField(name: String): FieldSymbol? {
        return lookup(name) as? FieldSymbol
    }

    fun resolveMethod(name: String): MethodSymbol? {
        return lookup(name) as? MethodSymbol
    }

    val declaredMembers: List<MemberSymbol>
        get() = symbols.filterIsInstance<MemberSymbol>()

    val declaredMethods: Set<MethodSymbol>
        get() = declaredMembers.filterIsInstance<MethodSymbol>().toSet()

    val declaredFields: Set<FieldSymbol>
        get() = declaredMembers.filterIsInstance<FieldSymbol>().toSet()

    val methods: Set<MethodSymbol>
        get() = buildSet {
            superClassScope?.let { addAll(it.methods) }
            addAll(declaredMethods)
        }

    val fields: Set<FieldSymbol>
        get() = buildSet {
            superClassScope?.let { addAll(it.fields) }
            addAll(declaredFields)
        }

    val members: Set<MemberSymbol>
        get() = buildSet {
            addAll(fields)
            addAll(methods)
        }

    override fun toString(): String = "Class($name)"
}