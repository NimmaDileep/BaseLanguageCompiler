package edu.udel.blc.util.uranium


import com.google.common.collect.HashMultimap
import java.util.*

/**
 * A reactor is a collection of [Rule]s that export attribute values, given some
 * attribute values as input. It also maintains a store of [Attribute] values.
 *
 * Rules can be registered using the builder returned by the [rule] method.
 *
 * Simple attributes values can be set using [set], but most attributes will be set
 * automatically when the rules are run.
 *
 * Call the [run] method to start the SymbolTable. This will run every applicable rule,
 * until no more rules can be run.
 *
 * During the execution, rules may generate errors. These errors may be retrieved
 * using [errors] (for "root" errors) or [allErrors] to get all errors, including derived
 * errors (where we couldn't compute the value of an attribute because there was another error
 * preventing the computation of a dependency).
 */
open class Reactor {

    val attributeValues = mutableMapOf<Attribute, Any>()

    private val dependents = HashMultimap.create<Attribute, Rule>()

    private val queue = ArrayDeque<Rule>()
    private val errors = mutableSetOf<Throwable>()
    private val attributelessDerivedErrors = mutableSetOf<Throwable>()
    private var running = false

    operator fun <T> get(attribute: Attribute): T = attributeValues[attribute] as T

    operator fun <T> get(obj: Any, name: String): T = get(Attribute(obj, name))

    /**
     * Return a stream of all the (attribute, value) pairs for attributes on the given node.
     */
    fun attributesFor(node: Any): Map<Attribute, Any> {
        return attributeValues.filterKeys { it.value === node }
    }

    val attributes: Set<Attribute>
        get() = attributeValues.keys

    /**
     * Returns the list of root errors encountered while running the SymbolTable. These are errors that
     * are not caused by another error, excepted for derived errors that are not attached to a
     * particular attribute.
     */
    fun errors(): Set<Throwable> {
        return errors
    }

    /**
     * Set the value of the given attribute that can be known statically, **before running the
     * reactor**. This is not meant for use in rules (use [Rule.set]).
     *
     * @throws IllegalStateException if called while the reactor is running
     */
    operator fun set(attribute: Attribute, value: Any) {
        require(!running) { "Calling Reactor#set while the reactor is running." }
        check(attribute !in attributeValues) {
            "attempting to redefine: $attribute from ${attributeValues.getValue(attribute)} to $value"
        }
        attributeValues[attribute] = value
    }

    operator fun set(obj: Any, name: String, value: Any) {
        this[Attribute(obj, name)] = value
    }

    /**
     * Run all the rules that can be run (directly, or transitively, as rules make new attributes
     * available).
     */
    fun run() {
        running = true

        // queue all rules with no dependencies
        dependents[NO_DEPS].forEach { rule -> queue.addLast(rule) }

        attributeValues.forEach { (attribute, value) ->
            dependents[attribute]
                .forEach { rule -> rule.supply(attribute, value) }
        }

        loopOnQueue()

        dependents.clear()
        running = false
    }

    fun enqueue(rule: Rule) {
        queue.addLast(rule)
    }

    /**
     * Loops in the queue until it is empty, running each queued rule and propagating
     * its exported values (which may in turn cause more rules to become enqueued).
     */
    private fun loopOnQueue() {
        while (!queue.isEmpty()) {
            val rule = queue.removeFirst()

            try {
                rule.run()
            } catch (e: Throwable) {
                throw RuntimeException("exception while running: $rule", e)
            }

            rule.exports.forEach { attribute ->
                val value = rule.exportValues.getValue(attribute)
                setValue(attribute, value)
            }
        }
    }

    private fun setValue(attribute: Attribute, value: Any) {
        val old = attributeValues.putIfAbsent(attribute, value)
        when {
            old is Throwable -> {
                // For now: skip and keep the first reported error. In the future, might want to let the
                // user pick - but that required changing error propagation to change the whole chain.
            }
//            old != null -> error("attempting to redefine: $attribute from $old to $value")
            value is Throwable -> {
                if (value.cause == null) {
                    errors += value
                }

                // Propagate the error to the exported attributes of any rule that depends on the affected attribute.
                val missingDependencyError = Error("missing dependency $attribute", value)
                dependents[attribute]
                    .flatMapTo(mutableSetOf()) { it.exports }
                    .forEach { exported -> setValue(exported, missingDependencyError) }
            }
            else -> {
                // supply the value to any rules that depend on it
                dependents[attribute].forEach { r -> r.supply(attribute, value) }
            }
        }
    }

    fun error(error: Throwable, vararg affected: Attribute) {
        error(error, affected.asList())
    }

    fun error(error: Throwable, affected: Collection<Attribute> = emptySet()) {
        when {
            affected.isEmpty() -> when (error.cause) {
                null -> errors += error
                else -> attributelessDerivedErrors += error
            }
            else -> affected.forEach { setValue(it, error) }
        }
    }


    fun register(rule: Rule) {
        when {
            rule.dependencies.isEmpty() -> {
                dependents.put(NO_DEPS, rule)
                if (running) {
                    enqueue(rule)
                }
            }
            else -> {
                rule.dependencies.forEach { dependency ->
                    dependents.put(dependency, rule)
                    if (running) {
                        attributeValues[dependency]?.let { value -> rule.supply(dependency, value) }
                    }
                }
            }
        }
    }


    fun rule(name: String = "", builderAction: Rule.Builder.() -> Unit) {
        val rule = Rule.Builder(name, this).apply(builderAction).build()
        register(rule)
    }

    fun <T : Any> map(name: String = "", from: Attribute, to: Attribute, f: (value: T) -> Any) {
        rule(name) {
            exports(to)
            using(from)
            by { r -> r[to] = f(r[from]) }
        }
    }

    fun copy(name: String = "", from: Attribute, to: Attribute) {
        map<Any>(name, from, to) { it }
    }

    fun <T : Any> flatMap(name: String = "", from: Iterable<Attribute>, to: Attribute, f: (values: List<T>) -> Any) {
        rule(name) {
            exports(to)
            using(from)
            by { r -> r[to] = f(from.map { r[it] }) }
        }
    }

    fun <T : Any> on(name: String = "", attribute: Attribute, consumer: (T) -> Unit) {
        rule(name) {
            using(attribute)
            by { r -> consumer(r[attribute]) }
        }
    }

    companion object {
        private val NO_DEPS = Attribute(Unit, "NO_DEPS")
    }
}