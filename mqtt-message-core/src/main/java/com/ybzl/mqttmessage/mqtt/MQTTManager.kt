package com.ybzl.mqttmessage.mqtt

import android.content.Context
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.NetworkUtils
import com.peter.mqtt.PeterMqttManager
import com.peter.mqtt.bean.ConnectState
import com.ybzl.mqttmessage.bean.ActionMessage
import com.ybzl.mqttmessage.bean.TopicMessage
import com.ybzl.mqttmessage.bean.YbMessageBusChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MQTTManager {

    private val mainScope = CoroutineScope(Dispatchers.Main)

    companion object {
        @Volatile
        private var instance: MQTTManager? = null
        fun getInstance(): MQTTManager {
            return instance ?: synchronized(this) {
                instance ?: MQTTManager().also { instance = it }
            }
        }
    }

    fun init(
        context: Context,
        serviceUrl: String,
        userName: String? = null,
        passwd: String? = null,
        isDebug: Boolean = false
    ) {
        PeterMqttManager.getInstance().init(
            context,
            serviceUrl,
            DeviceUtils.getUniqueDeviceId("YBZL_"),
            userName,
            passwd,
            isDebug
        ).subscribePermanentTopics(Pair("screen/setControl", 1))

        PeterMqttManager.listenConnectState { state ->
            updateConnectState(
                when (state) {
                    ConnectState.CONNECTED -> YbMessageBusChannel.ConnectState.CONNECTED
                    ConnectState.CONNECTING -> YbMessageBusChannel.ConnectState.CONNECTING
                    ConnectState.DISCONNECT -> YbMessageBusChannel.ConnectState.DISCONNECT
                }
            )
        }

        PeterMqttManager.listen { msgTopic ->
            mainScope.launch {
                if (msgTopic.topic.contains("screen/setControl")) {
                    val actionMessage =
                        GsonUtils.fromJson(msgTopic.message, ActionMessage::class.java)
                    val ip = NetworkUtils.getIPAddress(true)
                    //判断是否控制本机
                    if (actionMessage?.data?.device?.equIp == ip) {
                        YbMessageBusChannel.sendEvent(actionMessage)
                    }
                } else {
                    //为了每次能正常发送，增加时间戳字段
                    val data =
                        TopicMessage(msgTopic.topic, msgTopic.message, System.currentTimeMillis())
                    YbMessageBusChannel.sendEvent(data)
                }
            }

        }
    }


    fun subScribe(removeHistory: Boolean, vararg topic: Pair<String, Int>) {
        PeterMqttManager.getInstance().subscribeToTopic(removeHistory, *topic)
    }

    private fun unSubScribeAll() {
        PeterMqttManager.getInstance().subscribeToTopic(true)
    }

    /**
     * @param topic
     * @param payload
     * @param qos //有效值 0  1  2
     * @param retained //retained–服务器是否应保留此消息
     */
    fun publish(topic: String, payload: String, qos: Int = 0, retained: Boolean = false) {
        PeterMqttManager.getInstance().publish(topic, payload, qos, retained)
    }

    /**
     * @param topic
     * @param byteArray
     * @param qos //有效值 0  1  2
     * @param retained //retained–服务器是否应保留此消息
     */
    fun publish(topic: String, byteArray: ByteArray, qos: Int = 0, retained: Boolean = false) {
        PeterMqttManager.getInstance().publish(topic, byteArray, qos, retained)
    }

    private fun updateConnectState(state: YbMessageBusChannel.ConnectState) {
        mainScope.launch {
            YbMessageBusChannel.updateConnectState(state)
        }
    }

}
