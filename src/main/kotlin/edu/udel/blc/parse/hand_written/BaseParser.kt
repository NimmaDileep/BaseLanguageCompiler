package edu.udel.blc.parse.hand_written

import edu.udel.blc.ast.*
import edu.udel.blc.ast.BinaryOperator.*
import edu.udel.blc.ast.UnaryOperator.LOGICAL_COMPLEMENT
import edu.udel.blc.ast.UnaryOperator.NEGATION
import edu.udel.blc.parse.SyntaxError
import edu.udel.blc.parse.hand_written.BaseToken.Kind.*

class BaseParser(
    private val tokens: Iterator<BaseToken>
) {

    constructor(lexer: BaseLexer) : this(lexer.tokens())

    private var currentToken: BaseToken = tokens.next()

    private fun advance(): BaseToken {
        val previousToken = currentToken
        if (!isAtEnd) {
            currentToken = tokens.next()
        }
        return previousToken
    }

    private val isAtEnd: Boolean
        get() = currentToken.kind == EOF

    fun compilationUnit(): CompilationUnitNode {
        return CompilationUnitNode(0..0, declarations())
    }

    fun declarations() = buildList {
        while (!isAtEnd) {
            this += declaration()
        }
    }

    fun declaration(): StatementNode {
        return when {
            check(FUN) -> functionDeclaration()
            check(STRUCT) -> structDeclaration()
            check(VAR) -> variableDeclaration()
            else -> statement()
        }
    }

    fun functionDeclaration(): FunctionDeclarationNode {
        val keyword = consume(FUN) { "Expect 'fun'." }
        val name = consume(IDENTIFIER) { "Expect function name." }
        consume(LPAREN) { "Expect '(' after function name." }
        val parameters = list(RPAREN, ::parameter)
        consume(RPAREN) { "Expect ')' after parameters." }
        consume(ARROW) { "Expect '->' before return type" }
        val returnType = type()
        val body = block()
        return FunctionDeclarationNode(keyword.range, name.text, parameters, returnType, body)
    }

    fun parameter(): ParameterNode {
        val name = consume(IDENTIFIER) { "Expect parameter name." }
        consume(COLON) { "Expect ':' after parameter name" }
        val type = type()
        return ParameterNode(name.range, name.text, type)
    }

    fun structDeclaration(): StructDeclarationNode {
        val keyword = consume(STRUCT) { "Expect 'struct'." }
        val name = consume(IDENTIFIER) { "Expect struct name." }
        consume(LBRACE) { "Expect '{' before fields." }
        val fields = list(RBRACE, ::field)
        consume(RBRACE) { "Expect '}' after fields." }
        return StructDeclarationNode(name.range, name.text, fields)
    }

    fun field(): FieldNode {
        val name = consume(IDENTIFIER) { "Expect field name." }
        consume(COLON) { "Expect ':' after field name" }
        val type = type()
        return FieldNode(name.range, name.text, type)
    }

    fun variableDeclaration(): StatementNode {
        val keyword = consume(VAR) { "Expect 'var'." }
        val name = consume(IDENTIFIER) { "Expect variable name." }
        consume(COLON) { "Expect ':' after variable name." }
        val type = type()
        consume(EQUAL) { "Expect '=' before initializer." }
        val initializer = expression()
        consume(SEMICOLON) { "Expect ';' after variable declaration." }
        return VariableDeclarationNode(name.range, name.text, type, initializer)
    }


    fun statement(): StatementNode {
        return when {
            check(IF) -> ifStatement()
            check(RETURN) -> returnStatement()
            check(WHILE) -> whileStatement()
            check(LBRACE) -> block()
            else -> expressionStatement()
        }
    }

    fun block(): BlockNode {
        val lbrace = consume(LBRACE) { "Expect '{'." }
        val declarations = buildList {
            while (!check(RBRACE) && !isAtEnd) {
                this += declaration()
            }
        }
        consume(RBRACE) { "Expect '}'." }
        return BlockNode(lbrace.range, declarations)
    }

    fun expressionStatement(): StatementNode {
        val expr = expression()
        consume(SEMICOLON) { "Expect ';' after statement." }
        return ExpressionStatementNode(expr.range, expr)
    }

    fun ifStatement(): StatementNode {
        val keyword = consume(IF) { "Expect 'if'." }
        consume(LPAREN) { "Expect '(' after 'if'." }
        val condition = expression()
        consume(RPAREN) { "Expect ')' after if condition." }
        val thenStatement = statement()
        val elseStatement = when {
            match(ELSE) -> statement()
            else -> null
        }
        return IfNode(keyword.range, condition, thenStatement, elseStatement)
    }

    fun returnStatement(): StatementNode {
        val keyword = consume(RETURN) { "Expect 'return'." }
        val value = when {
            check(SEMICOLON) -> null
            else -> expression()
        }
        consume(SEMICOLON) { "Expect ';' after return." }
        return ReturnNode(keyword.range, value)
    }

    fun whileStatement(): StatementNode {
        val keyword = consume(WHILE) { "Expect 'while'." }
        consume(LPAREN) { "Expect '(' after 'while'." }
        val condition = expression()
        consume(RPAREN) { "Expect ')' after condition." }
        val body = statement()
        return WhileNode(keyword.range, condition, body)
    }


    fun expression(): ExpressionNode {
        return assignment()
    }

    private fun assignment(): ExpressionNode {
        var expr = disjunction()
        if (check(EQUAL)) {
            val operator = consume(EQUAL) { " Expect '='." }
            val value = assignment()
            expr = AssignmentNode(operator.range, expr, value)
        }
        return expr
    }

    private fun disjunction(): ExpressionNode {
        var expr = conjunction()
        while (check(OR)) {
            val operator = consume(OR) { "Expect '||'." }
            val right = conjunction()
            expr = BinaryExpressionNode(operator.range, LOGICAL_CONJUNCTION, expr, right)
        }
        return expr
    }

    private fun conjunction(): ExpressionNode {
        var expr = equality()
        while (check(AND)) {
            val operator = consume(AND) { "Expect '&&'." }
            val right = equality()
            expr = BinaryExpressionNode(operator.range, LOGICAL_CONJUNCTION, expr, right)
        }
        return expr
    }

    private fun equality(): ExpressionNode {
        var expr = comparison()
        while (true) {
            expr = when {
                check(BANG_EQUAL) -> {
                    val operator = consume(BANG_EQUAL) { "Expect '!='." }
                    val right = comparison()
                    BinaryExpressionNode(operator.range, NOT_EQUAL_TO, expr, right)
                }
                check(EQUAL_EQUAL) -> {
                    val operator = consume(EQUAL_EQUAL) { "Expect '=='." }
                    val right = comparison()
                    BinaryExpressionNode(operator.range, EQUAL_TO, expr, right)
                }
                else -> break
            }
        }
        return expr
    }

    private fun comparison(): ExpressionNode {

        var expr = term()

        while (true) {
            expr = when {
                check(RANGLE) -> {
                    val operator = consume(RANGLE) { "Expect '<'." }
                    val right = term()
                    BinaryExpressionNode(operator.range, GREATER_THAN, expr, right)
                }
                check(RANGLE_EQUAL) -> {
                    val operator = consume(RANGLE_EQUAL) { "Expect '<='." }
                    val right = term()
                    BinaryExpressionNode(operator.range, GREATER_THAN_OR_EQUAL_TO, expr, right)
                }
                check(LANGLE) -> {
                    val operator = consume(LANGLE) { "Expect '<'." }
                    val right = term()
                    BinaryExpressionNode(operator.range, LESS_THAN, expr, right)
                }
                check(LANGLE_EQUAL) -> {
                    val operator = consume(LANGLE_EQUAL) { "Expect '<='." }
                    val right = term()
                    BinaryExpressionNode(operator.range, LESS_THAN_OR_EQUAL_TO, expr, right)
                }
                else -> break
            }
        }

        return expr

    }

    private fun term(): ExpressionNode {

        var expr = factor()

        while (true) {
            expr = when {
                check(MINUS) -> {
                    val operator = consume(MINUS) { "Expect '-'." }
                    val right = factor()
                    BinaryExpressionNode(operator.range, SUBTRACTION, expr, right)
                }
                check(PLUS) -> {
                    val operator = consume(MINUS) { "Expect '+'." }
                    val right = factor()
                    BinaryExpressionNode(operator.range, ADDITION, expr, right)
                }
                else -> break
            }
        }

        return expr

    }

    private fun factor(): ExpressionNode {

        var expr = unaryPrefix()

        while (true) {
            expr = when {
                check(PERCENT) -> {
                    val operator = consume(PERCENT) { "Expect '%'." }
                    val right = factor()
                    BinaryExpressionNode(operator.range, REMAINDER, expr, right)
                }
                check(STAR) -> {
                    val operator = consume(STAR) { "Expect '*'." }
                    val right = factor()
                    BinaryExpressionNode(operator.range, MULTIPLICATION, expr, right)
                }
                else -> break
            }
        }

        return expr
    }

    private fun unaryPrefix(): ExpressionNode {
        return when {
            check(BANG) -> {
                val operator = consume(BANG) { "Expect '!'." }
                val right = unaryPrefix()
                UnaryExpressionNode(operator.range, LOGICAL_COMPLEMENT, right)
            }
            check(MINUS) -> {
                val operator = advance()
                val right = unaryPrefix()
                UnaryExpressionNode(operator.range, NEGATION, right)
            }
            else -> unaryPostfix()
        }
    }

    private fun unaryPostfix(): ExpressionNode {
        var expr = primary()

        while (true) {
            expr = when {
                check(LPAREN) -> {
                    val lparen = consume(LPAREN) { "Expect '('." }
                    val arguments = list(RPAREN, ::expression)
                    consume(RPAREN) { "Expect ')' after arguments." }
                    CallNode(lparen.range, expr, arguments)
                }
                check(LBRACKET) -> {
                    val lbracket = consume(LBRACKET) { "Expect '['." }
                    val index = expression()
                    consume(RBRACKET) { "Expect ']' after index." }
                    IndexNode(lbracket.range, expr, index)
                }
                check(DOT) -> {
                    val dot = consume(DOT) { "Expect '.'." }
                    val name = consume(IDENTIFIER) { "Expect name" }
                    FieldSelectNode(dot.range, expr, name.text)
                }
                else -> break
            }
        }

        return expr
    }

    fun primary(): ExpressionNode {
        return when {
            check(FALSE, TRUE) -> booleanLiteral()
            check(NUMBER) -> intLiteral()
            check(QUOTE_OPEN) -> stringLiteral()
            check(IDENTIFIER) -> identifier()
            check(UNIT) -> unitLiteral()
            check(LPAREN) -> parenthesizedExpression()
            check(LBRACKET) -> arrayLiteral()
            else -> error("Expect expression.")
        }
    }

    fun booleanLiteral(): BooleanLiteralNode {
        val literal = consume(TRUE, FALSE) { "Expect 'true' or 'false'." }
        return BooleanLiteralNode(literal.range, literal.text.toBoolean())
    }

    fun intLiteral(): IntLiteralNode {
        val literal = consume(NUMBER) { "Expect number." }
        return IntLiteralNode(literal.range, literal.text.toLong())
    }

    fun stringLiteral(): StringLiteralNode {
        val openQuote = consume(QUOTE_OPEN) { "Expect '\"'." }
        val literal = buildString {
            while (check(CHARACTER)) {
                val c = consume(CHARACTER) { "Expect character." }
                append(c.text)
            }
            consume(QUOTE_CLOSE) { "Expect '\"'." }
        }
        return StringLiteralNode(openQuote.range, literal)
    }

    fun identifier(): ReferenceNode {
        val name = consume(IDENTIFIER) { "Expect identifier." }
        return ReferenceNode(name.range, name.text)
    }

    fun parenthesizedExpression(): ExpressionNode {
        val lparen = consume(LPAREN) { "Expect '('." }
        val expr = expression()
        consume(RPAREN) { "Expect ')'." }
        return expr
    }

    fun arrayLiteral(): ArrayLiteralNode {
        val lbrace = consume(LBRACKET) { "Expect '['." }
        val elements = list(RBRACKET, ::expression)
        consume(RBRACKET) { "Expect ']'." }
        return ArrayLiteralNode(lbrace.range, elements)
    }

    fun unitLiteral(): UnitLiteralNode {
        val literal = consume(UNIT) { "Expect 'unit'." }
        return UnitLiteralNode(literal.range)
    }

    fun type(): Node {
        return when {
            check(IDENTIFIER) -> identifier()
            check(LBRACKET) -> arrayType()
            else -> error("Expect type.")
        }
    }

    fun arrayType(): ArrayTypeNode {
        val lbrace = consume(LBRACKET) { "Expect '[' after array type." }
        val elementType = type()
        consume(RBRACKET) { "Expect ']' after array type." }
        return ArrayTypeNode(lbrace.range, elementType)
    }

    fun <T> list(kind: BaseToken.Kind, element: () -> T): List<T> = buildList {
        if (!check(kind)) {
            do {
                when {
                    check(kind) -> break
                    else -> add(element())
                }
            } while (match(COMMA))
        }
    }

    private fun match(vararg kinds: BaseToken.Kind): Boolean {
        if (check(*kinds)) {
            advance()
            return true
        }
        return false
    }

    private fun consume(vararg kinds: BaseToken.Kind, lazyMessage: () -> String): BaseToken {
        when {
            check(*kinds) -> return advance()
            else -> error(lazyMessage())
        }
    }

    private fun check(vararg kinds: BaseToken.Kind): Boolean {
        return !isAtEnd && kinds.any { currentToken.kind == it }
    }

    private fun error(message: String): Nothing {
        throw SyntaxError(currentToken.range, "$message $currentToken")
    }

}
