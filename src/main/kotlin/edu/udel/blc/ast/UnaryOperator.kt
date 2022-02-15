package edu.udel.blc.ast

enum class UnaryOperator {

    NEGATION,
    LOGICAL_COMPLEMENT;

    override fun toString() = when (this) {
        NEGATION -> "-"
        LOGICAL_COMPLEMENT -> "!"
    }

}