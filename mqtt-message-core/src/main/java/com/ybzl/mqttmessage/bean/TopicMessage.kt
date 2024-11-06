package com.ybzl.mqttmessage.bean

import com.ybzl.mqttmessage.bean.Event

data class TopicMessage(
    val topic: String,
    val message: String? = null,
    val time: Long = 0
) : Event


