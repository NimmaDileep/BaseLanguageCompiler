package edu.udel.blc.semantic_analysis

import edu.udel.blc.BaseError
import edu.udel.blc.ast.Node

class SemanticError(
    node: Node,
    message: String,
    cause: Throwable? = null,
) : BaseError(node.range, message, cause)