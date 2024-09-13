package com.ybzl.network.eventbus

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch


object EventBusChannelFix {
    private val _sharedFlow = MutableSharedFlow<Event>(replay = 0, extraBufferCapacity = 1)
    private val _stickSharedFlow = MutableSharedFlow<Event>(replay = 1, extraBufferCapacity = 1)
    val sharedFlow: SharedFlow<Event> get() = _sharedFlow
    val stickSharedFlow: SharedFlow<Event> get() = _stickSharedFlow
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    fun sendEvent(event: Event) {
        coroutineScope.launch {
            _sharedFlow.emit(event)
        }
    }

    fun sendStickEvent(event: Event) {
        coroutineScope.launch {
            _stickSharedFlow.emit(event)
        }
    }

}

