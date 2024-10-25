package com.ybzl.track

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList


object TrackingManager {

    private lateinit var context: Application
    private var coroutineScope = CoroutineScope(Dispatchers.Main)


    fun init(context: Application) {
        this.context = context
    }

    val gson by lazy {
        Gson()
    }

    fun addTracking(trackingEvent: TrackingEvent) {
        coroutineScope.launch {
            TrackingEventDataBase.getInstance(context).insert(trackingEvent)
        }
    }


    suspend fun trackingList(): List<TrackingEventDBModel> {
        return TrackingEventDataBase.getInstance(context).queryAll()
    }

    fun getAllEventCodes(): List<String> {
        return TrackingEventDataBase.getInstance(context).trackingEventDao().getAllEventCodes()
    }

    suspend fun queryEventCode(eventCode: String?): List<TrackingEventDBModel> {
        if (eventCode == null) return emptyList()
        return TrackingEventDataBase.getInstance(context).trackingEventDao()
            .queryEventCode(eventCode) ?: emptyList()
    }

    fun clearEvent() {
        coroutineScope.launch {
            TrackingEventDataBase.getInstance(context).clearEvent()
        }

    }

}