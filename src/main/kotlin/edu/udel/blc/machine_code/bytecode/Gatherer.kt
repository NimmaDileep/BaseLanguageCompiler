package edu.udel.blc.machine_code.bytecode

import com.google.common.graph.Traverser
import edu.udel.blc.ast.*
import edu.udel.blc.util.visitor.ReflectiveAccessorSuccessors


inline fun <reified T : Node> Node.find(): List<T> {
    return Traverser.forTree(ReflectiveAccessorSuccessors(Node::class.java)).breadthFirst(this)
        .filterIsInstance<T>()
}