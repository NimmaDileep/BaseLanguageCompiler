package edu.udel.blc.ast.opt

import edu.udel.blc.ast.*
import edu.udel.blc.util.visitor.ValuedVisitor

class ASTClone: ValuedVisitor<Node, Node>() {
    init {
        register(ArrayLiteralNode::class.java, ::arrayLiteral)
        register(ArrayTypeNode::class.java, ::arrayType)
        register(AssignmentNode::class.java, ::assignment)
        register(BinaryExpressionNode::class.java, ::binaryExpression)
        register(BlockNode::class.java, ::block)
        register(BooleanLiteralNode::class.java, ::booleanLiteral)
        register(CallNode::class.java, ::call)
        register(CompilationUnitNode::class.java, ::compilationUnit)
        register(ExpressionStatementNode::class.java, ::expressionStatement)
        register(FieldNode::class.java, ::field)
        register(FieldSelectNode::class.java, ::fieldSelect)
        register(FunctionDeclarationNode::class.java, ::functionDeclaration)
        register(IfNode::class.java, ::ifStmt)
        register(IndexNode::class.java, ::index)
        register(IntLiteralNode::class.java, ::intLiteral)
        register(ParameterNode::class.java, ::parameter)
        register(ReferenceNode::class.java, ::reference)
        register(ReturnNode::class.java, ::returnStmt)
        register(StringLiteralNode::class.java, ::stringLiteral)
        register(StructDeclarationNode::class.java, ::structDeclaration)
        register(UnaryExpressionNode::class.java, ::unaryExpression)
        register(UnitLiteralNode::class.java, ::unitLiteral)
        register(VariableDeclarationNode::class.java, ::variableDeclaration)
        register(WhileNode::class.java, ::whileStmt)
    }

    private fun arrayLiteral(node: ArrayLiteralNode): ArrayLiteralNode = ArrayLiteralNode(
        range = node.range,
        elements = node.elements.map { apply(it) as ExpressionNode }
    )

    private fun arrayType(node: ArrayTypeNode): ArrayTypeNode = ArrayTypeNode(
        range = node.range,
        elementType = apply(node.elementType)
    )

    private fun assignment(node: AssignmentNode): AssignmentNode = AssignmentNode(
        range = node.range,
        lvalue = apply(node.lvalue) as ExpressionNode,
        expression = apply(node.expression) as ExpressionNode
    )

    private fun binaryExpression(node: BinaryExpressionNode): BinaryExpressionNode = BinaryExpressionNode(
        range = node.range,
        operator = node.operator,
        left = apply(node.left) as ExpressionNode,
        right = apply(node.right) as ExpressionNode
    )

    private fun block(node: BlockNode): BlockNode = BlockNode(
        range = node.range,
        statements = node.statements.map { apply(it) as StatementNode }
    )

    private fun booleanLiteral(node: BooleanLiteralNode): BooleanLiteralNode = BooleanLiteralNode(
        range = node.range,
        value = node.value
    )

    private fun call(node: CallNode): CallNode = CallNode(
        range = node.range,
        callee = apply(node.callee) as ExpressionNode,
        arguments = node.arguments.map { apply(it) as ExpressionNode }
    )

    private fun compilationUnit(node: CompilationUnitNode): CompilationUnitNode = CompilationUnitNode(
        range = node.range,
        statements = node.statements.map { apply(it) as StatementNode }
    )

    private fun expressionStatement(node: ExpressionStatementNode): ExpressionStatementNode = ExpressionStatementNode(
        range = node.range,
        expression = apply(node.expression) as ExpressionNode
    )

    private fun variableDeclaration(node: VariableDeclarationNode): VariableDeclarationNode = VariableDeclarationNode(
        range = node.range,
        name = node.name,
        type = node.type,
        initializer = apply(node.initializer) as ExpressionNode
    )

    private fun whileStmt(node: WhileNode): WhileNode = WhileNode(
        range = node.range,
        condition = apply(node.condition) as ExpressionNode,
        body = apply(node.body) as StatementNode
    )

    private fun functionDeclaration(node: FunctionDeclarationNode): FunctionDeclarationNode = FunctionDeclarationNode(
        range = node.range,
        name = node.name,
        parameters = node.parameters,
        returnType = node.returnType,
        body = apply(node.body) as BlockNode
    )

    private fun ifStmt(node: IfNode): IfNode = IfNode(
        range = node.range,
        condition = apply(node.condition) as ExpressionNode,
        thenStatement = apply(node.thenStatement) as StatementNode,
        elseStatement = node.elseStatement?.let { apply(it) } as StatementNode?
    )

    private fun index(node: IndexNode): IndexNode = IndexNode(
        range = node.range,
        expression = apply(node.expression) as ExpressionNode,
        index = apply(node.index) as ExpressionNode
    )

    private fun unitLiteral(node: UnitLiteralNode): UnitLiteralNode = UnitLiteralNode(
        range = node.range
    )

    private fun stringLiteral(node: StringLiteralNode): StringLiteralNode = StringLiteralNode(
        range = node.range,
        value = node.value
    )

    private fun field(node: FieldNode): FieldNode = FieldNode(
        range = node.range,
        name = node.name,
        type = apply(node.type)
    )

    private fun fieldSelect(node: FieldSelectNode): FieldSelectNode = FieldSelectNode(
        range =  node.range,
        name = node.name,
        expression = apply(node.expression) as ExpressionNode
    )

    private fun intLiteral(node: IntLiteralNode): IntLiteralNode = IntLiteralNode(
        range = node.range,
        value = node.value
    )

    private fun parameter(node: ParameterNode): ParameterNode = ParameterNode(
        range = node.range,
        name = node.name,
        type = apply(node.type)
    )

    private fun reference(node: ReferenceNode): ReferenceNode = ReferenceNode(
        range = node.range,
        name = node.name
    )

    private fun returnStmt(node: ReturnNode): ReturnNode = ReturnNode(
        range = node.range,
        expression = node.expression?.let { apply(node.expression) } as ExpressionNode?
    )

    private fun structDeclaration(node: StructDeclarationNode): StructDeclarationNode = StructDeclarationNode(
        range = node.range,
        name = node.name,
        fields = node.fields.map { apply(it) as FieldNode }
    )

    private fun unaryExpression(node: UnaryExpressionNode): UnaryExpressionNode = UnaryExpressionNode(
        range = node.range,
        operator = node.operator,
        operand = apply(node.operand) as ExpressionNode
    )

}