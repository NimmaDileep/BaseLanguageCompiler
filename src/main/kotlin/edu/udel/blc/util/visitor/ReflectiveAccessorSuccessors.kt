package edu.udel.blc.util.visitor

import com.google.common.graph.SuccessorsFunction
import com.google.common.reflect.TypeParameter
import com.google.common.reflect.TypeToken
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Modifier


class ReflectiveAccessorSuccessors<T : Any>(
    val clazz: Class<T>,
) : SuccessorsFunction<T> {

    private val iterableType: TypeToken<Iterable<T>> = object : TypeToken<Iterable<T>>() {}
        .where(object : TypeParameter<T>() {}, clazz)

    private val handleCache: MutableMap<Class<*>, List<Wrapper>> = mutableMapOf()

    private fun handlesFor(klass: Class<*>): List<Wrapper> =
        klass.methods
            .filter { it.parameterCount == 0 }
            .filter { !Modifier.isStatic(it.modifiers) }
            .mapNotNull { handle ->
                val returnType = TypeToken.of(handle.genericReturnType)

                when {
                    returnType.isSubtypeOf(clazz) -> Wrapper(lookup.unreflect(handle), false)
                    returnType.isSubtypeOf(iterableType) -> Wrapper(lookup.unreflect(handle), true)
                    else -> null
                }
            }

    override fun successors(node: T): Iterable<T> =
        handleCache.computeIfAbsent(node::class.java) { handlesFor(it) }
            .flatMap { (handle, isIterable) ->
                when (isIterable) {
                    true -> (handle.invoke(node) as Iterable<T?>).filterNotNull()
                    false -> listOfNotNull((handle.invoke(node) as T?))
                }
            }

    private data class Wrapper(val handle: MethodHandle, val isIterable: Boolean)

    companion object {
        @JvmStatic
        private val lookup: MethodHandles.Lookup = MethodHandles.lookup()
    }
}