package edu.udel.blc.parse.hand_written

import edu.udel.blc.Failure
import edu.udel.blc.Result
import edu.udel.blc.Success
import edu.udel.blc.ast.CompilationUnitNode
import edu.udel.blc.parse.Parser
import edu.udel.blc.parse.SyntaxError


class HandWrittenParser : Parser("Hand-written Parser") {

    override fun apply(source: String): Result<CompilationUnitNode> {
        return try {
            val lexer = BaseLexer(source)
            val parser = BaseParser(lexer)
            val compilationUnit = parser.compilationUnit()
            return Success(compilationUnit)
        } catch (error: SyntaxError) {
            Failure(setOf(error))
        }
    }

}

