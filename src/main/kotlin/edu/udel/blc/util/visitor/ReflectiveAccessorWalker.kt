package edu.udel.blc.util.visitor

import com.google.common.reflect.TypeToken
import java.lang.reflect.Modifier


/**
 * Implementation of [Walker] where the children of a node of type `T` are take to be
 * all accessible (public) zero-argument methods of the node whose return value is assignable to
 * `T`, or are instances of [Collection] parameterized with with type `T`. Static
 * methods are ignored.
 *
 * @see ReflectiveAccessorWalker ReflectiveAccessorWalker for something similar that uses
 * fields instead of accessor methods.
 */
class ReflectiveAccessorWalker<T : Any>(
    clazz: Class<T>,
    vararg visitTypes: WalkVisitType
) : ReflectiveWalker<T>(clazz, visitTypes.toSet()) {

    override fun handlesFor(klass: Class<*>): List<Wrapper> =
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

}