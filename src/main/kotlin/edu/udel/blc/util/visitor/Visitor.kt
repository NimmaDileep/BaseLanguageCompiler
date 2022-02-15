package edu.udel.blc.util.visitor

import java.util.function.Consumer

open class Visitor<T : Any> : Consumer<T> {

    private val dispatch: MutableMap<Class<out T>, Consumer<in T>> = mutableMapOf()
    private var fallbackSpecialization: Consumer<in T> = Consumer<T> { }

    override fun accept(value: T) {
        when (val action = dispatch[value::class.java]) {
            null -> fallbackSpecialization.accept(value)
            else -> action.accept(value)
        }
    }

    fun <S : T> register(klass: Class<S>, specialization: Consumer<in S>): Visitor<T> {
        dispatch[klass] = specialization as Consumer<in T>
        return this
    }

    fun registerFallback(fallback: Consumer<in T>): Visitor<T> {
        fallbackSpecialization = fallback
        return this
    }
}