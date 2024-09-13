package com.ybzl.track

import okhttp3.Interceptor
import okhttp3.Response

class TrackingResponseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // 构建 TrackingEvent
        val trackingEvent = TrackingEvent.NetworkRequestEvent(url = request.url.toString())

        // 获取请求的开始时间
        val startTime = System.currentTimeMillis()

        try {
            // 发送请求并获取响应
            val response = chain.proceed(request)

            // 获取请求的结束时间
            val endTime = System.currentTimeMillis()

            // 保存响应状态和响应数据
            trackingEvent.put("response_code", response.code)
            trackingEvent.put("response_time", endTime - startTime)

            // 处理追踪事件（例如，存入数据库或日志）
            TrackingManager.addTracking(trackingEvent)

            return response
        } catch (e: Exception) {
            // 如果请求失败，记录错误信息
            trackingEvent.put("error", e.message ?: "Unknown error")

            // 处理追踪事件
            TrackingManager.addTracking(trackingEvent)

            throw e // 继续抛出异常
        }
    }
}
