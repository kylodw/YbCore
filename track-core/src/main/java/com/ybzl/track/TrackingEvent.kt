package com.ybzl.track

import android.net.Uri
import android.util.Log
import androidx.room.Ignore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.net.URL
import java.util.HashMap
import java.util.concurrent.ConcurrentHashMap


sealed class TrackingEvent(@Transient val eventCode: String) {
    fun toJson(): String = TrackingManager.gson.toJson(this)
    private val parameters: ConcurrentHashMap<String, Any?> = ConcurrentHashMap()

    @Transient
    val timestamp: Long = System.currentTimeMillis()

    fun put(key: String, value: Any?) {
        parameters[key] = value
    }

    fun put(map: Map<String, Any?>) {
        parameters.putAll(map)
    }

    data class ClickEvent(
        val screenName: String,
        val componentName: String,
    ) : TrackingEvent("ClickEvent") {

    }

    data class NetworkRequestEvent(
        val url: String,
    ) : TrackingEvent("NetworkRequestEvent")

    data class MqttEvent(
        val serviceUri: String?,
        val topic: String? = null,
    ) : TrackingEvent("MqttEvent")

    data class ScreenEvent(
        val route: String
    ) : TrackingEvent("ScreenEvent") {
        init {
            Log.d("TrackingManager", toJson())
        }
    }

    data class ErrorEvent(val source: String, val message: String) : TrackingEvent("ErrorEvent")

    data class AsrEvent(
        val text:String,
        val score:Int
    ):TrackingEvent("AsrEvent")

    data class LogEvent(
        val message:String
    ):TrackingEvent("LogEvent")

}
