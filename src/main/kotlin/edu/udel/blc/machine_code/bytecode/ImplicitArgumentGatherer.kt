package edu.udel.blc.machine_code.bytecode

import edu.udel.blc.ast.FunctionDeclarationNode
import edu.udel.blc.ast.ReferenceNode
import edu.udel.blc.semantic_analysis.scope.FunctionSymbol
import edu.udel.blc.semantic_analysis.scope.Symbol
import edu.udel.blc.semantic_analysis.scope.VariableSymbol
import edu.udel.blc.util.uranium.Reactor
import java.util.function.BiFunction


object ImplicitArgumentGatherer : BiFunction<Reactor, FunctionDeclarationNode, List<ReferenceNode>> {

    override fun apply(reactor: Reactor, functionDeclaration: FunctionDeclarationNode): List<ReferenceNode> {

        val functionSymbol = reactor.get<FunctionSymbol>(functionDeclaration, "symbol")

        return functionDeclaration.find<ReferenceNode>()
            .filter { node ->
                val symbol = reactor.get<Symbol>(node, "symbol")
                symbol is VariableSymbol && functionSymbol !in symbol.containingScope.enclosingPath
            }
    }

}