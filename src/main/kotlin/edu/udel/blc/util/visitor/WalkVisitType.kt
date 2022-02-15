package edu.udel.blc.util.visitor

/**
 * Types of visit operations that can be performed by a [Walker].
 */
enum class WalkVisitType {
    /**
     * Call the visitor operation before visiting the node's children.
     */
    PRE_VISIT,

    /**
     * Call the visitor operation after visiting the node's children.
     */
    POST_VISIT,

}