package edu.udel.blc.util.visitor

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import edu.udel.blc.util.visitor.WalkVisitType.*
import java.util.function.BiConsumer
import java.util.function.Consumer

/**
 * An analog to [Visitor] that implements a tree walker that calls an operation
 * on every node in the tree hiearchy. The operation that has different behaviours
 * (*specializations*) for different subclasses of `T` (the node type), as well as
 * for different [visit types][WalkVisitType].
 *
 *
 * The walker can call operations on each node before visiting its children ([ ][WalkVisitType.PRE_VISIT]), after visiting its children ([WalkVisitType.POST_VISIT]), or
 * in-between every pair of successive children ([WalkVisitType.IN_VISIT]). It's possible to
 * combine multiple visit modalities, and all those that apply should be passed to [ ][.Walker].
 *
 *
 * Each specialization is represented by an instance of [Consumer]. They are registered by
 * calling [.register]. Specializations for a class are only
 * operational for values that have that specific class — inheritance does not enter into account
 * when dispatching the operation. Each specialization is also specific to one of the visit type
 * mentionned above.
 *
 *
 * It's also possible to specify a specialization applicable for all visit types. In this case,
 * the [WalkVisitType] will be passed as a parameter to the specialization, which has type
 * [BiConsumer]`<WalkVisitType, T>`. Use method [.registerFallback]
 * for this. If you choose to use this modality for a given class, you must not also use per-visit
 * specialization for that class!
 *
 *
 * If a specialization for a (class, visit type) combination does not exist, a fallback
 * specialization can be called. Fallback specializations are registered by calling [ ][.registerFallback] (per-visit-type fallbacks) and [ ][.registerFallback] (generic fallback). If a fallback can't be found, an [ ] is thrown.
 *
 *
 * The class offers the [.walk] method, which calls the visitor operation on the node
 * (possibly multiple times, see below) and recursively calls itself on the children of the
 * node.
 *
 *
 * To use this class, you must subclass it and override the [.children] method to
 * instruct the walker how to find the children of a node.
 *
 *
 * Specialization must not call the [.walk] method of their own walker!
 */
abstract class Walker<T : Any>(
    private val visitTypes: Set<WalkVisitType>
) {

    init {
        require(visitTypes.isNotEmpty()) { "no visit type provided" }
    }

    private val dispatch: Table<Class<T>, WalkVisitType, Consumer<T>> = HashBasedTable.create()
    private val fallbacks = mutableMapOf<WalkVisitType, Consumer<T>>(
        PRE_VISIT to Consumer {},
        POST_VISIT to Consumer {},
    )

    abstract fun successors(node: T): Iterable<T>

    fun accept(node: T) {

        if (PRE_VISIT in visitTypes) {
            dispatch.get(node::class.java, PRE_VISIT)?.accept(node)
                ?: fallbacks[PRE_VISIT]?.accept(node)
                ?: throw IllegalStateException("")
        }

        successors(node).forEach { accept(it) }

        if (POST_VISIT in visitTypes) {
            dispatch.get(node::class.java, POST_VISIT)?.accept(node)
                ?: fallbacks[POST_VISIT]?.accept(node)
                ?: throw IllegalStateException("")
        }
    }

    fun <S : T> register(klass: Class<S>, visitType: WalkVisitType, specialization: Consumer<in S>): Walker<*> {
        dispatch.put(klass as Class<T>, visitType, specialization as Consumer<T>)
        return this
    }

    fun registerFallback(visitType: WalkVisitType, fallback: Consumer<in T>): Walker<*> {
        fallbacks[visitType] = fallback as Consumer<T>
        return this
    }

}