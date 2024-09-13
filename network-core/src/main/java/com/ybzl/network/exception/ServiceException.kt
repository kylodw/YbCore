package com.ybzl.network.exception


sealed class AppException(val code: Int?, open val msg: String?) :
    Exception("code:${code},msg:${msg}") {
    class ServiceException(code: Int?, msg: String?) : AppException(code, msg)

    data object NoLoginException : AppException(ErrorCode.NO_LOGIN_CODE, "未登录")

    data object RequestNetworkException : AppException(ErrorCode.NETWORK_ERROR_CODE, "网络异常")

    data object CancellationException : AppException(ErrorCode.CANCELLATION_EX_CODE, "协程取消")

    data class RequestParamsException(override val msg: String) :
        AppException(ErrorCode.REQUEST_PARAMS_EX_CODE, msg)
}

