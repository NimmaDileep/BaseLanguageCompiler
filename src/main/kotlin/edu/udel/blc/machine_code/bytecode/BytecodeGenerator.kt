package edu.udel.blc.machine_code.bytecode

import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import edu.udel.blc.Result
import edu.udel.blc.Success
import edu.udel.blc.ast.CompilationUnitNode
import edu.udel.blc.machine_code.MachineCode
import edu.udel.blc.machine_code.MachineCodeGenerator
import edu.udel.blc.util.uranium.Reactor


class BytecodeGenerator : MachineCodeGenerator("Bytecode Generator") {

    override val extension: String = "jar"

    private val run by option("--run", help = "run the compiled bytecode").flag(default = true, defaultForHelp = "true")

    private val mainClassName by option("--main-class").default("Main")

    override fun apply(reactor: Reactor, compilationUnit: CompilationUnitNode): Result<MachineCode> {

        val result = CompilationUnitTranslator(mainClassName, reactor).apply(compilationUnit)

        if (run) {
            try {
                result.run()
            } catch (t: Throwable) {
                t.printStackTrace(System.err)
            }
        }

        return Success(result)
    }
}