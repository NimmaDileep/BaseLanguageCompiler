package edu.udel.blc

abstract class BaseError(
    val range: IntRange,
    message: String,
    cause: Throwable?
) : Error(message, cause)