package edu.udel.blc.util

import edu.udel.blc.util.visitor.ReflectiveAccessorWalker
import edu.udel.blc.util.visitor.WalkVisitType
import edu.udel.blc.util.visitor.Walker


open class TreeFormatter<T : Any>(
    private val appendable: Appendable,
    private val walker: Walker<T>,
    private val format: (T) -> String,
) {

    init {
        walker.registerFallback(WalkVisitType.PRE_VISIT, this@TreeFormatter::preVisit)
        walker.registerFallback(WalkVisitType.POST_VISIT, this@TreeFormatter::postVisit)
    }

    var INDENT = 2

    private var indent = 0

    private fun preVisit(node: T) {
        appendable.append(" ".repeat(indent))
        appendable.append(format(node))
        indent += INDENT
        appendable.append("\n")
    }

    private fun postVisit(node: T) {
        indent -= INDENT
    }

    companion object {

        fun <T : Any> appendTo(
            appendable: Appendable,
            root: T,
            klass: Class<T>,
            formatter: (T) -> String = { it.toString() }
        ) {
            val walker = ReflectiveAccessorWalker(klass, WalkVisitType.PRE_VISIT, WalkVisitType.POST_VISIT)
            val treeFormatter = TreeFormatter(appendable, walker, formatter)
            treeFormatter.walker.accept(root)
        }
    }

}