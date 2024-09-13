package com.ybzl.network.model

import com.ybzl.network.exception.AppException
import java.io.Serializable


@SinceKotlin("1.3")
@JvmInline
public value class BaseResult<out T> @PublishedApi internal constructor(
    @PublishedApi
    internal val value: Any?
) : Serializable {

    val isSuccess: Boolean get() = value !is AppException


    val isFailure: Boolean get() = value is AppException


    inline fun getOrNull(): T? =
        when {
            isFailure -> null
            else -> value as T
        }

    fun exceptionOrNull(): AppException? =
        when (value) {
            is AppException -> value
            else -> null
        }


    // companion with constructors

    /**
     * Companion object for [Result] class that contains its constructor functions
     * [success] and [failure].
     */
    companion object {

        @JvmName("success")
        public inline fun <T> success(value: T): BaseResult<T> =
            BaseResult(value)

        @JvmName("failure")
        public inline fun <T> failure(exception: AppException?): BaseResult<T> =
            BaseResult(exception)
    }


}