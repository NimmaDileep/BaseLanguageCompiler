package edu.udel.blc.semantic_analysis

import edu.udel.blc.Failure
import edu.udel.blc.Result
import edu.udel.blc.Success
import edu.udel.blc.ast.CompilationUnitNode
import edu.udel.blc.semantic_analysis.scope.BuiltinScope
import edu.udel.blc.util.uranium.Reactor
import java.util.function.Function


object SemanticAnalysis : Function<CompilationUnitNode, Result<Reactor>> {

    override fun apply(compilationUnit: CompilationUnitNode): Result<Reactor> {

        val reactor = Reactor()
        BuiltinScope.populate(reactor)

        ResolveReferences(reactor)
            .andThen(ResolveTypes(reactor))
            .andThen(CheckInferred(reactor))
            .andThen(CheckTypes(reactor))
            .andThen(CheckReturns(reactor))
            .accept(compilationUnit)

//        for((attribute, value) in SymbolTable.attributes) {
//            println("$attribute : $value")
//        }

        val errors = reactor.errors()
        return when {
            errors.isEmpty() -> Success(reactor)
            else -> Failure(errors as Set<SemanticError>)
        }
    }

}