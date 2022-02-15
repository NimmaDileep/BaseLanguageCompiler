package edu.udel.blc.util.visitor

import com.google.common.reflect.TypeParameter
import com.google.common.reflect.TypeToken
import edu.udel.blc.util.visitor.ReflectiveWalker.Companion.lookup
import edu.udel.blc.util.visitor.ReflectiveWalker.Wrapper
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles


/**
 * Abstract class for the implementation of a [Walker] that determines the children
 * of a node via (fast) reflection.
 *
 * Subclasses must override [handlesFor] to return a list of [Wrapper]. Each wrapper contains a [MethodHandle]
 * which either returns an object of type `T`, or an iterable of items of type `T`. The walker will walk over the
 * elements in iteration order.
 *
 * The [MethodHandle] contained in the returned list can be obtained by using the [lookup] static field of this class.
 */
sealed class ReflectiveWalker<T : Any>(
    protected val clazz: Class<T>,
    visitTypes: Set<WalkVisitType>,
) : Walker<T>(visitTypes) {

    protected val iterableType: TypeToken<Iterable<T>> = object : TypeToken<Iterable<T>>() {}
        .where(object : TypeParameter<T>() {}, clazz)

    private val handleCache: MutableMap<Class<*>, List<Wrapper>> = mutableMapOf()

    protected abstract fun handlesFor(klass: Class<*>): List<Wrapper>

    override fun successors(node: T): Iterable<T> =
        handleCache.computeIfAbsent(node::class.java) { handlesFor(it) }
            .flatMap { (handle, isIterable) ->
                when (isIterable) {
                    true -> (handle.invoke(node) as Iterable<T?>).filterNotNull()
                    false -> listOfNotNull((handle.invoke(node) as T?))
                }
            }

    protected data class Wrapper(val handle: MethodHandle, val isIterable: Boolean)

    companion object {
        @JvmStatic
        protected val lookup: MethodHandles.Lookup = MethodHandles.lookup()
    }

}