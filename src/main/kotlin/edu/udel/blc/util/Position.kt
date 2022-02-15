package edu.udel.blc.util

data class Position(val line: Int, val column: Int) {

    override fun toString(): String = "$line:$column"

}