package edu.udel.blc.semantic_analysis.type

class ClassType(
    val name: String,
    val fieldTypes: LinkedHashMap<String, Type>,
    val methodTypes: LinkedHashMap<String, Type>,
    val superClass: ClassType?
) : Type {
    override fun toString(): String = buildString {
        append(name)
        (fieldTypes + methodTypes).entries.joinTo(this, prefix = "{", postfix = "}") { (name, type) -> "$name : $type"}
    }
}