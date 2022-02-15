package edu.udel.blc.parse.antlr.util

import org.antlr.v4.runtime.misc.Utils
import org.antlr.v4.runtime.tree.Tree
import org.antlr.v4.runtime.tree.Trees

object TreeUtils {
    /**
     * Platform dependent end-of-line marker
     */
    private val Eol = System.lineSeparator()

    /**
     * The literal indent char(s) used for pretty-printing
     */
    const val indent = "  "
    private var level = 0

    /**
     * Pretty print out a whole tree. [.getNodeText] is used on the node payloads to get the text
     * for the nodes. (Derived from Trees.toStringTree(....))
     */
    fun toPrettyTree(t: Tree, ruleNames: List<String>): String {
        level = 0
        return process(t, ruleNames)
            .replace("(?m)^\\s+$".toRegex(), "")
            .replace("\\r?\\n\\r?\\n".toRegex(), Eol)
    }

    fun Tree.children(): List<Tree> = buildList {
        for(i in 0 until childCount) {
            add(getChild(i))
        }
    }

    private fun process(tree: Tree, ruleNames: List<String>): String {
        return when (tree.childCount) {
            0 -> Utils.escapeWhitespace(Trees.getNodeText(tree, ruleNames), false)
            else -> {
                buildString {
                    append(lead(level))
                    level++
                    val s = Utils.escapeWhitespace(Trees.getNodeText(tree, ruleNames), false)
                    append("$s ")
                    (0 until tree.childCount).forEach { i ->
                        append(process(tree.getChild(i), ruleNames))
                    }
                    level--
                    append(lead(level))
                }
            }
        }
    }

    private fun lead(level: Int) = buildString {
        if (level > 0) {
            append(Eol)
            append(indent.repeat(level))
        }
    }
}