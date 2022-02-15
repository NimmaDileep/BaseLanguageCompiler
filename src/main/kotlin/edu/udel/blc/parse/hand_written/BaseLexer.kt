package edu.udel.blc.parse.hand_written

import edu.udel.blc.parse.hand_written.BaseToken.Kind.*


class BaseLexer(
    private val source: CharSequence
) {

    private var start: Int = 0
    private var current: Int = 0

    private val isAtEnd: Boolean
        get() = current >= source.length

    private val range: IntRange
        get() = start until current

    private val text: String
        get() = source.substring(range)

    private val peek: Char
        get() = when {
            isAtEnd -> '\u0000'
            else -> source[current]
        }

    private var inString = false

    fun tokens() = iterator {
        while (!isAtEnd) {
            start = current

            val c = advance()

            val kind = when (inString) {
                true -> scanString(c)
                false -> scanDefault(c)
            }

            if (kind !in setOf(WHITESPACE, COMMENT)) {
                yield(token(kind))
            }
        }

        yield(token(EOF))
    }

    private fun scanString(c: Char): BaseToken.Kind {
        return when {
            '"' == c -> {
                inString = false
                QUOTE_CLOSE
            }
            else -> {
                CHARACTER
            }
        }
    }

    private fun scanDefault(c: Char): BaseToken.Kind {
        return when {
            '(' == c -> LPAREN
            ')' == c -> RPAREN
            '{' == c -> LBRACE
            '}' == c -> RBRACE
            '[' == c -> LBRACKET
            ']' == c -> RBRACKET
            ',' == c -> COMMA
            '.' == c -> DOT
            ':' == c -> COLON
            '-' == c -> when {
                match('>') -> ARROW
                else -> MINUS
            }
            '+' == c -> PLUS
            '*' == c -> STAR
            '%' == c -> PERCENT
            ';' == c -> SEMICOLON
            '&' == c -> when {
                match('&') -> AND
                else -> UNKNOWN
            }
            '|' == c -> when {
                match('|') -> OR
                else -> UNKNOWN
            }
            '!' == c -> when {
                match('=') -> BANG_EQUAL
                else -> BANG
            }
            '=' == c -> when {
                match('=') -> EQUAL_EQUAL
                else -> EQUAL
            }
            '<' == c -> when {
                match('=') -> LANGLE_EQUAL
                else -> LANGLE
            }
            '>' == c -> when {
                match('=') -> RANGLE_EQUAL
                else -> RANGLE
            }
            '/' == c -> when {
                match('/') -> comment()
                else -> SLASH
            }
            isWhitespace(c) -> whitespace()
            '"' == c -> {
                inString = true
                QUOTE_OPEN
            }
            isDigit(c) -> number()
            isAlpha(c) -> identifier()
            else -> UNKNOWN
        }
    }

    private fun comment(): BaseToken.Kind {
        while ('\n' != peek && !isAtEnd) {
            advance()
        }
        return COMMENT
    }

    private fun identifier(): BaseToken.Kind {
        while (isAlphaNumeric(peek)) {
            advance()
        }
        return reserved[text] ?: IDENTIFIER
    }

    private fun number(): BaseToken.Kind {
        while (isDigit(peek)) {
            advance()
        }
        return NUMBER
    }

    private fun whitespace(): BaseToken.Kind {
        while (isWhitespace(peek)) {
            advance()
        }

        return WHITESPACE
    }

    private fun token(kind: BaseToken.Kind): BaseToken {
        return BaseToken(kind, text, range)
    }

    private fun advance(): Char {
        current++
        return source[current - 1]
    }

    private fun match(expected: Char): Boolean = when {
        isAtEnd -> false
        source[current] != expected -> false
        else -> {
            advance()
            true
        }
    }

    private fun isWhitespace(c: Char): Boolean = c in setOf(' ', '\t', '\r', '\n')

    private fun isAlpha(c: Char): Boolean = c in 'a'..'z' || c in 'A'..'Z' || c == '_'

    private fun isDigit(c: Char): Boolean = c in '0'..'9'

    private fun isAlphaNumeric(c: Char): Boolean = isAlpha(c) || isDigit(c)

    companion object {
        private var reserved = mapOf(
            "fun" to FUN,
            "var" to VAR,
            "struct" to STRUCT,
            "if" to IF,
            "else" to ELSE,
            "return" to RETURN,
            "while" to WHILE,

            "true" to TRUE,
            "false" to FALSE,
            "unit" to UNIT,
        )
    }


}
