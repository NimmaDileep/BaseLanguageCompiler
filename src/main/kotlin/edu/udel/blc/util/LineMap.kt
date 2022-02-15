package edu.udel.blc.util

import java.util.*
import kotlin.math.max
import kotlin.math.min

class LineMap(
    private val name: String,
    private val content: String,
    private val lineStart: Int = 1,
    private val columnStart: Int = 1,
    private val tabSize: Int = 4,
) {

    private val linePositions: IntArray =
        buildList {
            add(0)
            content.withIndex()
                .filter { (_, c) -> c == '\n' }
                .mapTo(this) { (i, _) -> i + 1 }
        }.toIntArray()


    fun rangeOf(line: Int): IntRange {
        require(line >= lineStart && line - lineStart < linePositions.size) { "invalid line: $line" }

        return linePositions[line - lineStart]..when {
            line - lineStart == linePositions.size - 1 -> content.length
            else -> offsetFor(line + 1) - 1
        }
    }

    fun offsetFor(line: Int): Int {
        require(line >= lineStart && line - lineStart < linePositions.size) { "invalid line: $line" }
        return linePositions[line - lineStart]
    }

    fun endOffsetFor(line: Int): Int {
        require(line >= lineStart && line - lineStart < linePositions.size) { "invalid line: $line" }
        return when {
            line - lineStart == linePositions.size - 1 -> content.length
            else -> offsetFor(line + 1) - 1
        }
    }

    fun lineFrom(offset: Int): Int {
        require(offset in content.indices) { "invalid offset: $offset" }
        val index = Arrays.binarySearch(linePositions, offset)
        return when {
            index >= 0 -> index + lineStart
            else -> -index - 2 + lineStart
        }
    }

    private fun columnFrom(line: Int, offset: Int): Int {
        var col = 0
        (offsetFor(line) until offset).forEach { i ->
            col += when {
                content[i] == '\t' -> tabSize - col % tabSize
                else -> 1
            }
        }
        return col + columnStart
    }

    fun columnFrom(offset: Int): Int {
        val line = lineFrom(offset)
        return columnFrom(line, offset)
    }

    fun positionFrom(offset: Int): Position = Position(
        line = lineFrom(offset),
        column = columnFrom(lineFrom(offset), offset)
    )

    fun offsetFrom(position: Position): Int {
        val (line, column) = position
        require(line > lineStart && line - lineStart < linePositions.size) { "line $line" }

        val lineOffset = offsetFor(line)
        check(column >= columnStart) { "no column $column in line $line" }

        var columnOffset = 0
        var columnIndex = 0
        while (columnIndex + columnStart < column) {
            val c = content[lineOffset + columnOffset]

            columnIndex += when (c) {
                '\n' -> error("no column $column in line $line")
                '\t' -> tabSize - columnIndex % tabSize
                else -> 1
            }

            ++columnOffset
        }
        check(columnIndex + columnStart == column) { "column $column happens inside a tab" }

        return lineOffset + columnOffset
    }


    private fun getLine(lineNumber: Int): String {
        return content.substring(rangeOf(lineNumber))
    }

    fun lineSnippet(line: Int, column: Int, snippetLength: Int = 100): String {
        require(line >= lineStart && line - lineStart < linePositions.size) { "line $line" }

        val lineString = getLine(line).replace("\t".toRegex(), " ".repeat(tabSize))

        var start = 0
        var end = lineString.length
        if (end > snippetLength) {
            val len = snippetLength / 2 * 2 // for idiots who use odd numbers
            start = max(0, column - len / 2)
            end = min(lineString.length, column + len / 2)
            if (end - start < len) {
                when (start) {
                    0 -> end += len - (end - start)
                    else -> start -= len - (end - start)
                }
            }
        }

        check(column >= columnStart && columnStart + lineString.length >= column) { "no column $column in line $line" }

        return String.format(
            "%s%n%s^",
            lineString.substring(start, end).trimEnd(),
            " ".repeat(column - columnStart)
        )

    }

    fun lineSnippet(offset: Int, snippetLength: Int = 100): String {
        return lineSnippet(positionFrom(offset), snippetLength)
    }


    fun lineSnippet(position: Position, snippetLength: Int = 100): String {
        return lineSnippet(position.line, position.column, snippetLength)
    }

    fun stringWithName(offset: Int): String {
        return name + ":" + positionFrom(offset)
    }

}