package edu.udel.blc.semantic_analysis

import edu.udel.blc.ast.*
import edu.udel.blc.semantic_analysis.scope.CallableSymbol
import edu.udel.blc.semantic_analysis.type.FunctionType
import edu.udel.blc.semantic_analysis.type.UnitType
import edu.udel.blc.util.uranium.Attribute
import edu.udel.blc.util.uranium.Reactor
import edu.udel.blc.util.visitor.ReflectiveAccessorWalker
import edu.udel.blc.util.visitor.WalkVisitType.PRE_VISIT
import java.util.function.Consumer


class CheckReturns(
    val reactor: Reactor
) : Consumer<CompilationUnitNode> {

    val walker = ReflectiveAccessorWalker(Node::class.java, PRE_VISIT).apply {
        register(FunctionDeclarationNode::class.java, PRE_VISIT, ::functionDeclaration)
        register(IfNode::class.java, PRE_VISIT, ::ifStmt)
        register(ReturnNode::class.java, PRE_VISIT, ::returnStmt)
        register(BlockNode::class.java, PRE_VISIT, ::block)
    }

    override fun accept(compilationUnit: CompilationUnitNode) {
        walker.accept(compilationUnit)
        reactor.run()
    }

    private fun functionDeclaration(node: FunctionDeclarationNode) {
        reactor.on(
            name = "load function declaration symbol",
            attribute = Attribute(node, "symbol"),
        ) { symbol: CallableSymbol ->

            val symbolTypeAttribute = Attribute(symbol, "type")
            val bodyReturnsAttribute = Attribute(node.body, "returns")

            val returnTypeAttribute = if(node.returnType != null) Attribute(node.returnType, "type") else Attribute(node.body, "type")

            reactor.rule("check that function returns if necessary") {
                using(symbolTypeAttribute, bodyReturnsAttribute)
                by { r ->
                    val returnType = r.get<FunctionType>(symbolTypeAttribute).returnType
                    val returns = r.get<Boolean>(bodyReturnsAttribute)
                    if (!returns && returnType != UnitType) {
                        reactor.error(SemanticError(node, "missing return"))
                    }
                }
            }
        }
    }

    private fun block(node: BlockNode) {
        // a block returns if it contains something that can return and does return
        reactor.flatMap(
            name = "determine whether block returns",
            from = node.statements
                .filter { isReturnContainer(it) }
                .map { Attribute(it, "returns") },
            to = Attribute(node, "returns"),
        ) { bodyReturns: List<Boolean> -> bodyReturns.any { it } }
    }

    private fun ifStmt(node: IfNode) {
        // an if statement returns only when all of its arms return
        reactor.flatMap(
            name = "determine whether if returns",
            from = listOfNotNull(node.thenStatement, node.elseStatement)
                .filter { isReturnContainer(it) }
                .map { Attribute(it, "returns") },
            to = Attribute(node, "returns"),
        ) { branchReturns: List<Boolean> ->
            branchReturns.isNotEmpty() && branchReturns.all { it }
        }
    }

    private fun returnStmt(node: ReturnNode) {
        // Indicate that return statements return
        reactor[node, "returns"] = true
    }

    private fun isReturnContainer(node: Node): Boolean {
        return (node is BlockNode || node is IfNode || node is ReturnNode)
    }

}
