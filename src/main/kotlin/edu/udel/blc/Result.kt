package edu.udel.blc

sealed class Result<out V> {
    abstract operator fun component1(): V?
    abstract operator fun component2(): Set<BaseError>?
}

inline infix fun <V1, V2> Result<V1>.andThen(transform: (V1) -> Result<V2>): Result<V2> {
    return when (this) {
        is Success -> transform(value)
        is Failure -> this
    }
}

/**
 * Invokes [action] if this [Result] is [Success].
 */
inline infix fun <V> Result<V>.onSuccess(action: (V) -> Unit): Result<V> {
    if (this is Success) {
        action(value)
    }

    return this
}

/**
 * Invokes [action] if this [Result] is [Failure].
 */
inline infix fun <V> Result<V>.onFailure(action: (Set<BaseError>) -> Unit): Result<V> {
    if (this is Failure) {
        action(errors)
    }

    return this
}

/**
 * Represents a successful [TaskResult], containing a [value].
 */
class Success<out V>(
    val value: V
) : Result<V>() {

    override fun component1(): V = value
    override fun component2(): Nothing? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Success<*>

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = "Ok($value)"
}

/**
 * Represents a failed [Result], containing a list of [Error].
 */
class Failure(
    val errors: Set<BaseError>
) : Result<Nothing>() {

    override fun component1(): Nothing? = null
    override fun component2() = errors

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Failure

        if (errors != other.errors) return false

        return true
    }

    override fun hashCode(): Int = errors.hashCode()
    override fun toString(): String = "Failure($errors)"
}


inline fun <V> binding(crossinline block: ResultBinding.() -> V): Result<V> {

    val receiver = ResultBindingImpl()

    return try {
        with(receiver) { Success(block()) }
    } catch (ex: BindException) {
        receiver.error
    }
}

object BindException : Exception()

interface ResultBinding {
    fun <V> Result<V>.bind(): V
}

@PublishedApi
internal class ResultBindingImpl : ResultBinding {

    lateinit var error: Failure

    override fun <V> Result<V>.bind(): V {
        return when (this) {
            is Success -> value
            is Failure -> {
                this@ResultBindingImpl.error = this
                throw BindException
            }
        }
    }
}