package com.ybzl.network.extend

import com.ybzl.network.exception.AppException
import com.ybzl.network.exception.ErrorCode
import com.ybzl.network.model.BaseResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import okio.ProtocolException
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException


suspend fun <T : AResponseModelFix<*>> safeApiCallFix(
    call: suspend () -> T,
    onSuccess: (suspend (T) -> Unit)? = null,
    handleException: ((AppException) -> Unit)? = null,
): BaseResult<T> {
    return try {
        val result = withContext(Dispatchers.IO) {
            call.invoke()
        }
        when (result.code) {
            ErrorCode.SUCCESS_CODE, ErrorCode.SUCCESS_CODE_0 -> {
                onSuccess?.invoke(result)
                BaseResult.success(result)
            }

            ErrorCode.NO_LOGIN_CODE -> {
                BaseResult.failure(AppException.NoLoginException)
            }

            else -> {
                BaseResult.failure(
                    AppException.ServiceException(
                        result.code,
                        "ERROR:${result.code}"
                    )
                )
            }
        }

    } catch (e: Exception) {
        when (e) {
            is ConnectException, is SocketException, is SocketTimeoutException, is UnknownHostException,
            is SSLHandshakeException, is MalformedURLException, is ProtocolException, is IOException,
            -> {
                handleException?.invoke(AppException.RequestNetworkException)
                BaseResult.failure(AppException.RequestNetworkException)
            }

            is CancellationException -> {
                // 协程被取消
                handleException?.invoke(AppException.CancellationException)
                BaseResult.failure(AppException.CancellationException)
            }

            else -> {
                handleException?.invoke(
                    AppException.ServiceException(
                        ErrorCode.OTHER_ERROR_CODE,
                        e.message
                    )
                )
                BaseResult.failure(
                    AppException.ServiceException(
                        ErrorCode.OTHER_ERROR_CODE,
                        e.message
                    )
                )
            }
        }

    }
}

