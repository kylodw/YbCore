package com.ybzl.mqttmessage.bean

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter

interface Event

object YbMessageBusChannel {

    private val _sharedActionFlow = MutableSharedFlow<ActionMessage>(replay = 1, extraBufferCapacity = 1)
    val sharedActionFlow: SharedFlow<ActionMessage> get() = _sharedActionFlow

    private val _sharedRawDataFlow = MutableSharedFlow<TopicMessage>(replay = 1, extraBufferCapacity = 1)
    val sharedRawDataFlow: SharedFlow<TopicMessage> get() = _sharedRawDataFlow

    private val _sharedConnectStateFlow = MutableSharedFlow<ConnectState>(replay = 1, extraBufferCapacity = 1)
    val connectStateFlow: SharedFlow<ConnectState> get() = _sharedConnectStateFlow

    suspend fun sendEvent(event: Event) {
        if (event is ActionMessage) {
            _sharedActionFlow.emit(event)
        }
        if (event is TopicMessage) {
            _sharedRawDataFlow.emit(event)
        }
    }

    suspend fun updateConnectState(state: ConnectState) {
        _sharedConnectStateFlow.emit(state)
    }

    fun filterTopic(vararg topic: String): Flow<TopicMessage> {
        return sharedRawDataFlow.filter { tc ->
            tc.topic in topic
        }
    }

    sealed interface ConnectState {
        data object CONNECTED : ConnectState
        data object CONNECTING : ConnectState
        data object DISCONNECT : ConnectState
    }

}
