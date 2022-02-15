package edu.udel.blc.semantic_analysis.type

class ClassType(
    val name: String,
    val superClass: ClassType?
) : Type