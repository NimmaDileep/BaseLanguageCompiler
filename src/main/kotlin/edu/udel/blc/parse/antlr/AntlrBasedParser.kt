package edu.udel.blc.parse.antlr

import BaseLexer
import BaseParser
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import edu.udel.blc.Failure
import edu.udel.blc.Result
import edu.udel.blc.Success
import edu.udel.blc.ast.CompilationUnitNode
import edu.udel.blc.parse.Parser
import edu.udel.blc.parse.SyntaxError
import edu.udel.blc.parse.antlr.util.TreeUtils.toPrettyTree
import org.antlr.v4.runtime.*
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import java.util.*


class AntlrBasedParser : Parser("Antlr-based Parser"), ANTLRErrorListener {

    private val printParseTree by option("--print-parse-tree", help = "Print the parse tree")
        .flag(default = false, defaultForHelp = "false")

    val errors: MutableSet<SyntaxError> = mutableSetOf()

    override fun apply(source: String): Result<CompilationUnitNode> {

        val charStream = CharStreams.fromString(source)
        val lexer = BaseLexer(charStream)
        lexer.addErrorListener(this)

        val parser = BaseParser(BufferedTokenStream(lexer))
        parser.addErrorListener(this)

        val result = parser.compilatonUnit()

        if (printParseTree) {
            val prettyTree = toPrettyTree(result, parser.ruleNames.toList())
            println(prettyTree)
        }

        return when {
            errors.isEmpty() -> {
                val compilationUnit = result.accept(BaseVisitor()) as CompilationUnitNode
                return Success(compilationUnit)
            }
            else -> Failure(errors)
        }
    }

    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?
    ) {
        val range = (offendingSymbol as? Token)?.let { it.startIndex..it.stopIndex } ?: -1..-1
        errors += SyntaxError(range, msg ?: "")
    }

    override fun reportAmbiguity(
        recognizer: org.antlr.v4.runtime.Parser?,
        dfa: DFA?,
        startIndex: Int,
        stopIndex: Int,
        exact: Boolean,
        ambigAlts: BitSet?,
        configs: ATNConfigSet?
    ) {

    }

    override fun reportAttemptingFullContext(
        recognizer: org.antlr.v4.runtime.Parser?,
        dfa: DFA?,
        startIndex: Int,
        stopIndex: Int,
        conflictingAlts: BitSet?,
        configs: ATNConfigSet?
    ) {

    }

    override fun reportContextSensitivity(
        recognizer: org.antlr.v4.runtime.Parser?,
        dfa: DFA?,
        startIndex: Int,
        stopIndex: Int,
        prediction: Int,
        configs: ATNConfigSet?
    ) {

    }


}
