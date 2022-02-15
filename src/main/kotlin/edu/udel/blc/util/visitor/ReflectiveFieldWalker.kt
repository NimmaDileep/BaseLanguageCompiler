package edu.udel.blc.util.visitor

import com.google.common.reflect.TypeToken
import java.lang.reflect.Modifier

/**
 * Implementation of [Walker] where the children of a node of type `T` are all public fields of the node
 * whose value is assignable to `T`, or are instances of [Iterable<T>]. Static fields are ignored.
 *
 * @see ReflectiveAccessorWalker ReflectiveAccessorWalker for something similar that uses
 * accessor methods instead of fields.
 */
open class ReflectiveFieldWalker<T : Any>(
    clazz: Class<T>,
    vararg visitTypes: WalkVisitType
) : ReflectiveWalker<T>(clazz, visitTypes.toSet()) {

    override fun handlesFor(klass: Class<*>): List<Wrapper> =
        klass.fields
            .filter { !Modifier.isStatic(it.modifiers) }
            .mapNotNull { field ->
                val fieldType = TypeToken.of(field.genericType)

                when {
                    fieldType.isSubtypeOf(clazz) -> Wrapper(lookup.unreflectGetter(field), false)
                    fieldType.isSubtypeOf(iterableType) -> Wrapper(lookup.unreflectGetter(field), true)
                    else -> null
                }
            }

}