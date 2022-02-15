package edu.udel.blc.parse

import edu.udel.blc.BaseError

class SyntaxError(
    range: IntRange,
    message: String,
    cause: Throwable? = null
) : BaseError(range, message, cause)
