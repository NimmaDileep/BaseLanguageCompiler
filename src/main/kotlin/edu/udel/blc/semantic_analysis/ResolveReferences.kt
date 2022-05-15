package edu.udel.blc.semantic_analysis

import edu.udel.blc.ast.*
import edu.udel.blc.semantic_analysis.scope.*
import edu.udel.blc.util.uranium.Attribute
import edu.udel.blc.util.uranium.Reactor
import edu.udel.blc.util.visitor.ReflectiveAccessorWalker
import edu.udel.blc.util.visitor.WalkVisitType.POST_VISIT
import edu.udel.blc.util.visitor.WalkVisitType.PRE_VISIT
import java.util.function.Consumer


class ResolveReferences(
    private val reactor: Reactor,
) : Consumer<CompilationUnitNode> {

    private var scope: Scope = BuiltinScope

    val walker = ReflectiveAccessorWalker(Node::class.java, PRE_VISIT, POST_VISIT).apply {

        register(CompilationUnitNode::class.java, PRE_VISIT, ::enterCompilationUnit)
        register(CompilationUnitNode::class.java, POST_VISIT, ::exitCompilationUnit)

        register(FunctionDeclarationNode::class.java, PRE_VISIT, ::enterFunctionDeclaration)
        register(FunctionDeclarationNode::class.java, POST_VISIT, ::exitFunctionDeclaration)

        register(ParameterNode::class.java, PRE_VISIT, ::parameter)
        register(VariableDeclarationNode::class.java, PRE_VISIT, ::variableDeclaration)

        register(StructDeclarationNode::class.java, PRE_VISIT, ::enterStructDeclaration)
        register(StructDeclarationNode::class.java, POST_VISIT, ::exitStructDeclaration)

        register(ClassDeclarationNode::class.java, PRE_VISIT, ::enterClassDeclaration)
        register(ClassDeclarationNode::class.java, POST_VISIT, ::exitClassDeclaration)

        register(FieldNode::class.java, PRE_VISIT, ::field)

        register(BlockNode::class.java, PRE_VISIT, ::enterBlock)
        register(BlockNode::class.java, POST_VISIT, ::exitBlock)

        register(ReferenceNode::class.java, PRE_VISIT, ::reference)

        register(ReturnNode::class.java, PRE_VISIT, ::returnStmt)

        registerFallback(PRE_VISIT, ::enterNode)
    }

    override fun accept(compilationUnit: CompilationUnitNode) {
        walker.accept(compilationUnit)
        reactor.run()
    }

    private fun enterCompilationUnit(node: CompilationUnitNode) {
        reactor[node, "scope"] = scope

        val globalScope = GlobalScope(scope)
        scope = globalScope
    }

    private fun exitCompilationUnit(node: CompilationUnitNode) {
        scope = scope.containingScope!!
    }

    private fun enterFunctionDeclaration(node: FunctionDeclarationNode) {
        reactor[node, "scope"] = scope

        val functionScope = FunctionSymbol(node.name, scope)
        scope.declare(functionScope)

        reactor[node, "symbol"] = functionScope

        scope = functionScope
    }

    private fun exitFunctionDeclaration(node: FunctionDeclarationNode) {
        scope = scope.containingScope!!
    }

    private fun enterBlock(node: BlockNode) {
        reactor[node, "scope"] = scope

        val localScope = LocalScope(scope)

        scope = localScope
    }

    private fun exitBlock(node: BlockNode) {
        scope = scope.containingScope!!
    }

    private fun parameter(node: ParameterNode) {
        reactor[node, "scope"] = scope

        val symbol = VariableSymbol(node.name, scope)
        reactor[node, "symbol"] = symbol
        scope.declare(symbol)
    }

    private fun variableDeclaration(node: VariableDeclarationNode) {
        reactor[node, "scope"] = scope

        val symbol = VariableSymbol(node.name, scope)
        scope.declare(symbol)

        reactor[node, "symbol"] = symbol
    }

    private fun enterStructDeclaration(node: StructDeclarationNode) {
        reactor[node, "scope"] = scope

        val structSymbol = StructSymbol(node.name, scope)
        scope.declare(structSymbol)
        reactor[node, "symbol"] = structSymbol
        scope = structSymbol
    }

    private fun exitStructDeclaration(node: StructDeclarationNode) {
        scope = scope.containingScope!!
    }

    private fun enterClassDeclaration(node: ClassDeclarationNode) {
        reactor[node, "scope"] = scope

        // TODO: Update to include superclass in class symbol
        val classSymbol = ClassSymbol(
            node.name,
            containingScope = scope,
            superClassName = null
        )
        scope.declare(classSymbol)
        reactor[node, "symbol"] = classSymbol
        scope = classSymbol
    }

    private fun exitClassDeclaration(node: ClassDeclarationNode) {
        scope = scope.containingScope!!
    }

    private fun field(node: FieldNode) {
        reactor[node, "scope"] = scope

        val symbol = FieldSymbol(node.name, scope)
        scope.declare(symbol)
        reactor[node, "symbol"] = symbol
    }

    private fun enterNode(node: Node) {
        reactor[node, "scope"] = scope
    }

    private fun reference(node: ReferenceNode) {
        reactor[node, "scope"] = scope

        // attempt to resolve when encountering a reference
        when (val symbol = scope.lookup(node.name)) {
            // if it's not found, add a rule to try again after traversal is complete
            null -> reactor.map(
                from = Attribute(node, "scope"),
                to = Attribute(node, "symbol")
            ) { scope: Scope ->

                when (val symbol = scope.lookup(node.name)) {
                    // if the symbol is null or a variable symbol, report an error
                    // if we can find a variable now, but not before this means that the variable is used
                    // prior to being declared.
                    null, is VariableSymbol -> SemanticError(node, "unknown identifier: ${node.name}")
                    else -> symbol
                }
            }
            else -> reactor[node, "symbol"] = symbol
        }
    }

    private fun returnStmt(node: ReturnNode) {
        reactor[node, "scope"] = scope

        when (val function = containingFunction(scope)) {
            null -> reactor.error(SemanticError(node, "return outside of function"))
            else -> reactor[node, "containingFunction"] = function
        }
    }

    private fun containingFunction(start: Scope): FunctionSymbol? {
        var scope: Scope? = start

        while (scope != null) {
            if (scope is FunctionSymbol) return scope
            scope = scope.containingScope
        }

        return null
    }

}
