package com.ybzl.mqttmessage.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ybzl.mqttmessage.bean.ActionType
import com.ybzl.mqttmessage.bean.YbMessageBusChannel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ScreenMask() {

    var mtOnScreen by remember {
        mutableStateOf(false)
    }

    val actionMessage = YbMessageBusChannel.sharedActionFlow.collectAsState(initial = null)
    val action = actionMessage.value?.data?.action?.firstOrNull { it.actionType == ActionType.SCREEN_IS_ON }
    if (action != null && action.actionValue != "1" && mtOnScreen.not()) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable {
                mtOnScreen = true
                GlobalScope.launch {
                    delay(10000)
                    mtOnScreen = false
                }
            })
    }
}