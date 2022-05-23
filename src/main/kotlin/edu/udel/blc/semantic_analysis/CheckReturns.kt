package edu.udel.blc.semantic_analysis

import edu.udel.blc.ast.*
import edu.udel.blc.semantic_analysis.scope.CallableSymbol
import edu.udel.blc.semantic_analysis.type.FunctionType
import edu.udel.blc.semantic_analysis.type.Type
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

            if (node.returnType == null) {
                val parameterTypeAttributesMap = symbol.parameters
                    .associateTo(LinkedHashMap()) { parameterSymbol ->
                        parameterSymbol.name to Attribute(parameterSymbol, "type")
                    }

                val bodyReturnTypeAttribute = Attribute(node.body, "returnType")
                reactor.rule(
                    name = "infer the function return type",
                ) {
                    using(parameterTypeAttributesMap.values)
                    using(bodyReturnTypeAttribute)
                    exports(symbolTypeAttribute)
                    by {
                        r ->
                        r[symbolTypeAttribute] = FunctionType(
                            parameterTypes = parameterTypeAttributesMap
                                .mapValuesTo(LinkedHashMap()) { (_, fieldTypeAttribute) ->
                                    r[fieldTypeAttribute]
                                },
                            returnType = r[bodyReturnTypeAttribute]
                        )
                    }
                }
            } else {
                reactor.copy(
                    name = "resolve function return type",
                    from = Attribute(node.returnType, "type"),
                    to = Attribute(node, "returnType")
                )
            }

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
        val returnChildren = node.statements.filter { isReturnContainer(it) }

        reactor.flatMap(
            name = "determine whether block returns",
            from = returnChildren.map { Attribute(it, "returns") },
            to = Attribute(node, "returns"),
        ) { bodyReturns: List<Boolean> ->
            val returns = bodyReturns.any { it }

            if (returns) {
                val childrenTypeAttributes = returnChildren
                    .zip(bodyReturns)
                    .filter { (_, it) -> it }
                    .map { (node, _) -> Attribute(node, "returnType") }

                reactor.flatMap(
                    name = "infer return type of block",
                    from = childrenTypeAttributes,
                    to = Attribute(node, "returnType")
                ) { childrenReturns: List<Type> ->
                    childrenReturns.reduce { acc, type -> acc.commonSupertype(type) }
                }
            }

            returns
        }
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
            val returns = branchReturns.isNotEmpty() && branchReturns.all { it }

            if (returns) {
                // infer its return type
                reactor.flatMap(
                    name = "infer return type of if",
                    from = listOfNotNull(node.thenStatement, node.elseStatement)
                        .filter { isReturnContainer(it) }
                        .map { Attribute(it, "returnType") },
                    to = Attribute(node, "returnType")
                ) { branchReturnTypes: List<Type> ->
                    branchReturnTypes.reduce { acc, type -> acc.commonSupertype(type) }
                }
            }

            returns
        }
    }

    private fun returnStmt(node: ReturnNode) {
        // Indicate that return statements return
        reactor[node, "returns"] = true

        if (node.expression != null) {
            reactor.map(
                name = "infer return type of return",
                from = Attribute(node.expression, "type"),
                to = Attribute(node, "returnType")
            ) {
                type: Type -> type
            }
        } else {
            reactor[node, "returnType"] = UnitType
        }

    }

    private fun isReturnContainer(node: Node): Boolean {
        return (node is BlockNode || node is IfNode || node is ReturnNode)
    }

}
