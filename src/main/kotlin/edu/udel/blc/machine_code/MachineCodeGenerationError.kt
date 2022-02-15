package edu.udel.blc.machine_code

import edu.udel.blc.BaseError

class MachineCodeGenerationError(
    range: IntRange,
    message: String,
    cause: Throwable? = null
) : BaseError(range, message, cause)
