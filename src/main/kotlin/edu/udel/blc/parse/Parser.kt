package edu.udel.blc.parse

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import edu.udel.blc.Failure
import edu.udel.blc.Result
import edu.udel.blc.Success
import edu.udel.blc.ast.CompilationUnitNode
import java.util.function.Function


abstract class Parser(
    name: String
) : Function<String, Result<CompilationUnitNode>>, OptionGroup(name)