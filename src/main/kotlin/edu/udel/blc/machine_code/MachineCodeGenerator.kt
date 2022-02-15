package edu.udel.blc.machine_code

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import edu.udel.blc.Result
import edu.udel.blc.ast.CompilationUnitNode
import edu.udel.blc.util.uranium.Reactor
import java.util.function.BiFunction

abstract class MachineCodeGenerator(
    name: String
) : BiFunction<Reactor, CompilationUnitNode, Result<MachineCode>>, OptionGroup(name) {

    abstract val extension: String

}