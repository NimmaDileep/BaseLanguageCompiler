package edu.udel.blc.util.visitor

import java.util.function.Function

open class ValuedVisitor<T : Any, R> : Function<T, R> {

    private val dispatch = mutableMapOf<Class<out T>, Function<in T, out R>>()
    private var fallbackSpecialization: Function<in T, out R>? = null

    override fun apply(value: T): R {
        val action = dispatch[value::class.java]
        return if (action == null) if (fallbackSpecialization == null)
            error("no fallback specified for ${value::class.java} (offending value: $value)")
        else fallbackSpecialization!!.apply(value) else action.apply(value)
    }

    fun <T1 : T> register(klass: Class<T1>, specialization: Function<in T1, out R>): ValuedVisitor<T, R> {
        dispatch[klass] = specialization as Function<in T, out R>
        return this
    }

    fun registerFallback(fallback: Function<in T, out R>?): ValuedVisitor<T, R> {
        fallbackSpecialization = fallback
        return this
    }

}