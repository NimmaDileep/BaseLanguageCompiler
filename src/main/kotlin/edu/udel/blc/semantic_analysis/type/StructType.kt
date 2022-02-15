package edu.udel.blc.semantic_analysis.type

class StructType(
    val name: String,
    val fieldTypes: LinkedHashMap<String, Type>
) : Type {

    override fun toString(): String = buildString {
        append(name)
        fieldTypes.entries.joinTo(this, prefix = "{", postfix = "}") { (name, type) -> "$name : $type"}
    }

}
