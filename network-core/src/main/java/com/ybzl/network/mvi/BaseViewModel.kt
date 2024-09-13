package com.ybzl.network.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ybzl.network.eventbus.Event
import com.ybzl.network.eventbus.EventBusChannelFix
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {
    init {
        viewModelScope.launch {
            EventBusChannelFix.stickSharedFlow.collect {
                handleStickEvent(it)
            }
        }
        viewModelScope.launch {
            EventBusChannelFix.sharedFlow.collect {
                handleEvent(it)
            }
        }
    }

    open fun handleEvent(event: Event) {

    }

    open fun handleStickEvent(event: Event) {

    }

}

