package edu.udel.blc.ast

enum class BinaryOperator {

    ADDITION,
    SUBTRACTION,
    MULTIPLICATION,
    REMAINDER,

    EQUAL_TO,
    NOT_EQUAL_TO,

    GREATER_THAN,
    GREATER_THAN_OR_EQUAL_TO,
    LESS_THAN,
    LESS_THAN_OR_EQUAL_TO,

    LOGICAL_CONJUNCTION,
    LOGICAL_DISJUNCTION;

    override fun toString() = when (this) {
        ADDITION -> "+"
        SUBTRACTION -> "-"
        MULTIPLICATION -> "*"
        REMAINDER -> "%"

        EQUAL_TO -> "=="
        NOT_EQUAL_TO -> "!="

        GREATER_THAN -> ">"
        GREATER_THAN_OR_EQUAL_TO -> ">="
        LESS_THAN -> "<"
        LESS_THAN_OR_EQUAL_TO -> "<="

        LOGICAL_CONJUNCTION -> "&&"
        LOGICAL_DISJUNCTION -> "||"
    }
}