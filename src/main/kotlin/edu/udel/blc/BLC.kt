package edu.udel.blc

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.groups.defaultByName
import com.github.ajalt.clikt.parameters.groups.groupChoice
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import edu.udel.blc.ast.CompilationUnitNode
import edu.udel.blc.ast.Node
import edu.udel.blc.ast.opt.ExpressionOptimizer
import edu.udel.blc.machine_code.MachineCode
import edu.udel.blc.machine_code.bytecode.BytecodeGenerator
import edu.udel.blc.parse.antlr.AntlrBasedParser
import edu.udel.blc.parse.hand_written.HandWrittenParser
import edu.udel.blc.semantic_analysis.SemanticAnalysis
import edu.udel.blc.util.LineMap
import edu.udel.blc.util.TreeFormatter
import java.io.File

class BLC : CliktCommand() {

    init {
        context { helpFormatter = CliktHelpFormatter(showDefaultValues = true) }
    }

    private val input by argument().file(mustExist = true, mustBeReadable = true, canBeDir = false)

    private val parser by option("-p", "--parser")
        .groupChoice("antlr" to AntlrBasedParser(), "handwritten" to HandWrittenParser())
        .defaultByName("handwritten")

    private val target by option("-t", "--target")
        .groupChoice(
            "bytecode" to BytecodeGenerator(),
        )
        .defaultByName("bytecode")

    private val printAst by option("--print-ast", help = "Print the abstract syntax tree")
        .flag(default = false, defaultForHelp = "false")

    private val output by option("-o", "--output", help = "Location to store binary")
        .file(mustBeWritable = true, mustExist = false)

    private val constantFolding by option(
        "-f",
        "--fold-constants",
        help = "Optimizes the code by Constant Folding"
    )
        .flag("-n", "--no-fold-constants", default = true, defaultForHelp = "true")

    private val strengthReduction by option(
        "-r",
        "--reduce-strength",
        help = "Optimize the code by Strength Reduction"
    )
        .flag(default = true, defaultForHelp = "true")

    private val deadCodeElimination by option(
        "-e",
        "--eliminate-dead-code",
        help = "Optimize the code by Dead Code Elimination"
    )
        .flag(default = true, defaultForHelp = "true")

    private fun onSuccess(codeGenerationResult: MachineCode) {
        val outFile = output ?: File("${input.nameWithoutExtension}.${target.extension}")
        codeGenerationResult.writeTo(outFile)
    }

    private fun reportErrors(errors: Set<BaseError>) {
        val lineMap = LineMap(input.name, input.readText())
        errors.forEach { error ->
            val position = lineMap.stringWithName(error.range.first)
            System.err.println(lineMap.lineSnippet(error.range.first))
            System.err.println("[$position]: ${error.message}")
        }
    }

    override fun run() {

        val source = input.readText()

        val result = binding {
            var compilationUnit = parser.apply(source).bind()

            if (printAst) {
                TreeFormatter.appendTo(System.out, compilationUnit, Node::class.java)
            }

            val symboltable = SemanticAnalysis.apply(compilationUnit).bind()

            if (constantFolding) {
                compilationUnit = ExpressionOptimizer().apply(compilationUnit) as CompilationUnitNode
            }

            target.apply(symboltable, compilationUnit).bind()
        }

        result.onSuccess(::onSuccess).onFailure(::reportErrors)
    }
}

fun main(args: Array<String>) = BLC().main(args)
