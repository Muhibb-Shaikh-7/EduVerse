package com.example.eduverse.data.model

/**
 * Sealed Result class for handling success and error states
 */
sealed class DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>()
    data class Error(
        val exception: Exception,
        val message: String = exception.message ?: "Unknown error"
    ) : DataResult<Nothing>()

    object Loading : DataResult<Nothing>()

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Error

    val isLoading: Boolean
        get() = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun exceptionOrNull(): Exception? = when (this) {
        is Error -> exception
        else -> null
    }
}

/**
 * Extension function to map Result data
 */
inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> {
    return when (this) {
        is DataResult.Success -> DataResult.Success(transform(data))
        is DataResult.Error -> DataResult.Error(exception, message)
        is DataResult.Loading -> DataResult.Loading
    }
}

/**
 * Extension function to handle Result states
 */
inline fun <T> DataResult<T>.onSuccess(action: (T) -> Unit): DataResult<T> {
    if (this is DataResult.Success) {
        action(data)
    }
    return this
}

inline fun <T> DataResult<T>.onError(action: (Exception) -> Unit): DataResult<T> {
    if (this is DataResult.Error) {
        action(exception)
    }
    return this
}

inline fun <T> DataResult<T>.onLoading(action: () -> Unit): DataResult<T> {
    if (this is DataResult.Loading) {
        action()
    }
    return this
}
