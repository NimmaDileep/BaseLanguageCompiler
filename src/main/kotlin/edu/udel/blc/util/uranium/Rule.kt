package edu.udel.blc.util.uranium

import edu.udel.blc.util.uranium.Rule.Builder
import java.util.function.Consumer


/**
 * A rule computes a set of exported attributes, using a set of dependencies attribute values.
 *
 *
 * A `Rule` is created by calling the [Reactor.rule] method and specifying the exported
 * attributes ([Builder.exports] methods), dependencies attributes ([Builder.using] methods),
 * as well as the computation logic to go from one to the other ([Builder.by]. No value are
 * supplied at this stage.
 *
 * A `Rule` must not (and can not) be invoked manually, instead they are registered to a
 * [Reactor] which invokes them when appropriate.
 *
 * The language implementer can access the `Rule` object from the logic passed to [Builder.by].
 * It is used to access the value of dependencies attributes ([get]), set the value of exports
 * attributes [set], and report semantic errors ([error]).
 *
 * <h2>Rule Computation Contract</h2>
 *
 * A rule must supply the value of all its exported attributes ([set]), or signal that a semantic
 * error prevented their computation ([error]). Failure to do so will cause the [Reactor] to
 * throw an exception.
 *
 * When an error is signaled for an attribute, that error will be set as the value of attribute.
 * The error will be propagated (signaled) to rules that depend on the attribute (resulting to all
 * **their** exported attribues to be signaled as error too). The rule computation for these
 * rules is of course not triggered.
 *
 * It's also possible to signal an error that does not preclude the computation of any
 * attribute, by leaving the attribute list empty when calling [.errorFor].
 *
 * By default, multiple rules are not allowed to compete to provide the value of a single attribute.
 * However, this can be made possible by overriding the [Reactor.attributeRedefinitionAttempt] (which by
 * default throws an exception). This can notably be used to enable incremental attribute computation,
 * where each rule can be rerun as its dependency values change. In the default configuration, each rule
 * this guaranteed to be run at most once.
 *
 * <h2>Lazy/Chained Rules</h2>
 *
 *
 * It is possible to instantiate new rules from within a rule. We call this pattern "lazy rules"
 * or "chained rules". This is often necessary, because we need to dynamically loo kup a node before
 * we can use it as part of an attribute. For instance the node could be the result of a scope
 * lookup that allows use-before-declaration, or it could be the value of a computed attribute.
 *
 * Lazy rules are not special, they just happen to be instantiated later than the other rules.
 *
 * However, it is worth paying attention to reported errors when using lazy rules. In particular,
 * when an error early in a rule prevents the instantiation of a lazy rule, then you should signal
 * that the error prevents the computation of the lazy rule's exports, using the [errorFor] method.
 * You can also instantiate the rule regardless, and let the error be propagated to it.
 */
class Rule(
    val name: String,
    val reactor: Reactor,
    val exports: Set<Attribute>,
    val dependencies: Set<Attribute>,
    val computation: Consumer<Rule>
) {

    val dependencyValues = mutableMapOf<Attribute, Any>()
    val exportValues = mutableMapOf<Attribute, Any>()

    val isReady: Boolean
        get() = dependencies.all { it in dependencyValues }

    override fun toString(): String = buildString {
        append("Rule: ")
        append(name)
        append("\n")
        append("  exports: ")
        exports.joinTo(this, postfix = "\n") { export ->
            when (val value = exportValues[export]) {
                null -> "$export"
                else -> "$export = $value"
            }
        }
        append("  using: ")
        dependencies.joinTo(this, postfix = "\n") { dependency ->
            when (val value = dependencyValues[dependency]) {
                null -> "$dependency"
                else -> "$dependency = $value"
            }
        }
    }


    fun run() {
        check(isReady)
        computation.accept(this)

        val missingExports = exports.filter { it !in exportValues }
        if (missingExports.isNotEmpty()) {
            error("Rule($name) did not export attributes: $missingExports")
        }
    }

    /**
     * Called by the Reactor to supply a dependency value.
     */
    fun supply(dependency: Attribute, value: Any) {
        dependencyValues[dependency] = value
        if (isReady) {
            reactor.enqueue(this)
        }
    }

    operator fun <T> get(dependency: Attribute): T {
        require(dependency in dependencies) { "attempting to access undeclared: $dependency\n$this" }
        return dependencyValues[dependency] as T
    }

    /**
     * Sets the value of the given export.
     */
    operator fun set(export: Attribute, value: Any) {
        exportValues[export] = value
    }


    /**
     * Used to build a [Rule]. Create an instance by calling [Reactor.rule].
     */
    class Builder(
        private val name: String,
        private val reactor: Reactor
    ) {

        private val exports = mutableSetOf<Attribute>()
        private val dependencies = mutableSetOf<Attribute>()
        private var by: Consumer<Rule> = Consumer { }

        fun exports(attributes: Collection<Attribute>): Builder {
            exports += attributes
            return this
        }

        fun exports(vararg attributes: Attribute): Builder {
            return exports(attributes.asList())
        }

        /**
         * Specifies the dependencies of this rule. (optional)
         */
        fun using(attributes: Iterable<Attribute>): Builder {
            dependencies += attributes
            return this
        }

        fun using(vararg attributes: Attribute): Builder {
            return using(attributes.asList())
        }

        /**
         * Specifies how to compute the exported attributes from the dependency attributes.
         *
         * This should call [Rule.set] to set the value of each exported attribute.
         * Dependencies can be accessed through [Rule.get].
         */
        fun by(computation: Consumer<Rule>) {
            by = computation
        }

        fun build(): Rule {
            return Rule(name, reactor, exports, dependencies, by)
        }
    }

}