package com.ybzl.track

import com.google.gson.reflect.TypeToken
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response

class TrackingRequestInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // 获取请求信息
        val request = chain.request()

        // 创建 NetworkRequestEvent
        val trackingEvent = TrackingEvent.NetworkRequestEvent(url = request.url.toString())

        // 可选：你可以在这里添加更多的参数，如请求头、请求体等
        trackingEvent.put("method", request.method)
        // 处理不同请求方法的参数
        when (request.method) {
            "GET" -> {
                // GET 请求时获取查询参数
                val queryParams = request.url.queryParameterNames.associateWith {
                    request.url.queryParameter(it).orEmpty()
                }
                trackingEvent.put("query_params", queryParams)
            }

            "POST" -> {
                // POST 请求时获取请求体参数
                val bodyParams = request.body?.let { extractPostParams(it) }
                if (bodyParams != null) {
                    trackingEvent.put("body_params", bodyParams)
                }
            }
            // 可以根据需要扩展其他请求方法
        }
        TrackingManager.addTracking(trackingEvent)

        // 继续执行请求
        return chain.proceed(request)
    }

    private fun extractPostParams(body: RequestBody): Map<String, Any> {
        val buffer = okio.Buffer()
        body.writeTo(buffer)
        // 假设 POST 请求体是 JSON 或者 form-urlencoded 数据，按需解析
        return try {
            when (body.contentType()?.subtype) {
                "json" -> {
                    // 解析 JSON
                    val typeToken = object : TypeToken<Map<String, Any>>() {}.type
                    TrackingManager.gson.fromJson(buffer.readUtf8(), typeToken)
                }

                "x-www-form-urlencoded" -> {
                    // 解析 form-urlencoded
                    val params = mutableMapOf<String, Any>()
                    val paramPairs = buffer.readUtf8().split("&")
                    paramPairs.forEach {
                        val keyValue = it.split("=")
                        if (keyValue.size == 2) {
                            params[keyValue[0]] = keyValue[1]
                        }
                    }
                    params
                }

                else -> {
                    emptyMap()
                }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }


}