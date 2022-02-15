package edu.udel.blc.util.uranium

class Attribute(
    val value: Any,
    val name: String,
) {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Attribute -> false
        else -> value === other.value && name === other.name
    }

    override fun hashCode(): Int {
        return (31 * value.hashCode()) + name.hashCode()
    }

    override fun toString(): String {
        return "($value :: $name)"
    }

}