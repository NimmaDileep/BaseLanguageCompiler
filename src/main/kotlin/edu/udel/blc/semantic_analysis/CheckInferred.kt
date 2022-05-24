package edu.udel.blc.semantic_analysis

import edu.udel.blc.ast.*
import edu.udel.blc.semantic_analysis.scope.CallableSymbol
import edu.udel.blc.semantic_analysis.scope.FunctionSymbol
import edu.udel.blc.semantic_analysis.scope.VariableSymbol
import edu.udel.blc.semantic_analysis.type.Type
import edu.udel.blc.util.uranium.Attribute
import edu.udel.blc.util.uranium.Reactor
import edu.udel.blc.util.visitor.ReflectiveAccessorWalker
import edu.udel.blc.util.visitor.WalkVisitType.*
import java.util.function.Consumer

/**
 * Checks whether the types have been inferred for ndoes that may not be resolved in type
 */
class CheckInferred(
    val reactor: Reactor
): Consumer<CompilationUnitNode> {
   val walker = ReflectiveAccessorWalker(Node::class.java, PRE_VISIT).apply {
       register(VariableDeclarationNode::class.java, PRE_VISIT, ::variableDeclaration)
       register(FunctionDeclarationNode::class.java, PRE_VISIT, ::functionDeclaration)
       register(ArrayLiteralNode::class.java, PRE_VISIT, ::arrayLiteral)
       register(ReferenceNode::class.java, PRE_VISIT, ::reference)
   }

    private fun variableDeclaration(node: VariableDeclarationNode) {

        reactor.on(
            name = "check inferred variable declaration type",
            attribute = Attribute(node, "symbol")
        ) { symbol: VariableSymbol ->
            if (node.type != null) {
               reactor.copy(
                    name = "reassign variable type",
                    from = Attribute(node.type, "type"),
                    to = Attribute(symbol, "type")
                )
            }

            checkInferred(node, symbol)
        }

    }

    private fun functionDeclaration(node: FunctionDeclarationNode) {
        reactor.on(
            name = "check inferred function type",
            attribute = Attribute(node, "symbol")
        ) { symbol: CallableSymbol ->
            if(node.returnType != null) {
                reactor.copy(
                    name = "reassign variable type",
                    from = Attribute(node.returnType, "type"),
                    to = Attribute(symbol, "type")
                )
            }
            checkInferred(node, symbol)
        }
    }

    private fun arrayLiteral(node: ArrayLiteralNode) = checkInferred(node, node)

    private fun reference(node: ReferenceNode) = checkInferred(node, node)

    private fun checkInferred(node: Node, obj: Any) {
        if (reactor.get<Type?>(obj, "type") == null) {
            reactor.error(
                SemanticError(
                    node,
                    "unable to infer type for $obj"
                )
            )
        }
    }

    override fun accept(node: CompilationUnitNode) {
        walker.accept(node)
        reactor.run()
    }
}