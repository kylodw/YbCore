package com.ybzl.mqttmessage.mqtt

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.NetworkUtils
import com.ybzl.mqttmessage.R
import com.ybzl.mqttmessage.bean.ActionMessage
import com.ybzl.mqttmessage.bean.TopicMessage
import com.ybzl.mqttmessage.bean.YbMessageBusChannel
import info.mqtt.android.service.Ack
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.nio.charset.Charset

class MQTTManager {
    private val CHANNEL_ID = "YbMessageServiceChannel"
    private var mqttAndroidClient: MqttAndroidClient? = null
    private val mClientId: String = DeviceUtils.getUniqueDeviceId(false).plus(System.currentTimeMillis())
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val mTopics = mutableMapOf<String, Int>()
    private var isDebug = false

    companion object {
        @Volatile
        private var instance: MQTTManager? = null
        fun getInstance(): MQTTManager {
            return instance ?: synchronized(this) {
                instance ?: MQTTManager().also { instance = it }
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "医标通知服务",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(serviceChannel)
        }
    }

    fun init(
        context: Context, serviceUrl: String, isDebug: Boolean = false
    ) {
        this.isDebug = isDebug
        val mqttCallback: MqttCallbackExtended = object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                //连接成功
                log("连接成功")
                updateConnectState(YbMessageBusChannel.ConnectState.CONNECTED)
            }

            override fun connectionLost(cause: Throwable) {
                log("断开连接")
                //断开连接
                updateConnectState(YbMessageBusChannel.ConnectState.DISCONNECT)
                handleReconnect(context)
            }

            @Throws(Exception::class)
            override fun messageArrived(topic: String, message: MqttMessage) {
                log(topic, message)
                //得到的消息
                mainScope.launch {
                    val msg = message.payload?.toString(Charset.defaultCharset()) ?: ""
                    if (topic.contains("screen/setControl")) {
                        val actionMessage = GsonUtils.fromJson(msg, ActionMessage::class.java)
                        val ip = NetworkUtils.getIPAddress(true)
                        //判断是否控制本机
                        if (actionMessage?.data?.device?.equIp == ip) {
                            YbMessageBusChannel.sendEvent(actionMessage)
                        }
                    } else {
                        //为了每次能正常发送，增加时间戳字段
                        val data = TopicMessage(topic, msg, System.currentTimeMillis())
                        YbMessageBusChannel.sendEvent(data)
                    }
                }

            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                //发送消息成功后的回调
            }
        }
        mqttAndroidClient = MqttAndroidClient(context, serviceUrl, mClientId, Ack.AUTO_ACK).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(context)
                setForegroundService(createNotification(context))
            }
        }
        mqttAndroidClient?.setCallback(mqttCallback)
        connectMqtt(context)
    }

    private fun connectMqtt(context: Context) {
        updateConnectState(YbMessageBusChannel.ConnectState.CONNECTING)
        mqttAndroidClient?.connect(defaultOption(), object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                subScribe(false, *mTopics.map { Pair(it.key, it.value) }.toTypedArray())
                updateConnectState(YbMessageBusChannel.ConnectState.CONNECTED)
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                updateConnectState(YbMessageBusChannel.ConnectState.DISCONNECT)
                handleReconnect(context)
            }
        })
    }

    private fun createNotification(context: Context): Notification {

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("医标服务")
            .setContentText("消息服务正在运行中")
            .setSmallIcon(R.drawable.logo)
            .setAutoCancel(false)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setOngoing(true)
            .build()
    }

    fun subScribe(removeHistory: Boolean, vararg topic: Pair<String, Int>) {

        unSubScribeAll()

        if (removeHistory) {
            mTopics.clear()
        }
        mTopics.putAll(topic)

        //如果没有订阅设备亮熄屏，则强制订阅
        if (mTopics.map { c -> c.key }.contains("screen/setControl").not()) {
            mTopics["screen/setControl"] = 0
        }

        mainScope.launch {
            if (mqttAndroidClient?.isConnected == false) {
                delay(3000)
                subScribe(removeHistory, *topic)
                return@launch
            }
            topic.forEach {
                mqttAndroidClient?.subscribe(it.first, it.second)
            }
        }
    }

    private fun unSubScribeAll() {
        mainScope.launch {
            if (mqttAndroidClient?.isConnected == true) {
                mTopics.forEach {
                    mqttAndroidClient?.unsubscribe(it.key)
                }
            }
        }
    }

    /**
     * @param topic
     * @param payload
     * @param qos //有效值 0  1  2
     * @param retained //retained–服务器是否应保留此消息
     */
    fun publish(topic: String, payload: String, qos: Int = 0, retained: Boolean = false) {
        mqttAndroidClient?.publish(topic, payload.toByteArray(), qos, retained)
    }

    /**
     * @param topic
     * @param byteArray
     * @param qos //有效值 0  1  2
     * @param retained //retained–服务器是否应保留此消息
     */
    fun publish(topic: String, byteArray: ByteArray, qos: Int = 0, retained: Boolean = false) {
        mqttAndroidClient?.publish(topic, byteArray, qos, retained)
    }

    private fun defaultOption(): MqttConnectOptions {
        return MqttConnectOptions().apply {
            isAutomaticReconnect = true  // 自动重连
            isCleanSession = false       // 保持会话
        }
    }

    private fun handleReconnect(context: Context) {
        mainScope.launch {
            delay(5000)
            connectMqtt(context)
        }
    }

    private fun updateConnectState(state: YbMessageBusChannel.ConnectState) {
        mainScope.launch {
            YbMessageBusChannel.updateConnectState(state)
        }
    }

    private fun log(log: String) {
        if (isDebug) {
            Log.e("MQTTManager", log)
        }
    }

    private fun log(topic: String, mqttMessage: MqttMessage?) {
        if (isDebug) {
            Log.e("MQTTManager", "$topic,${mqttMessage.toString()}")
        }
    }

}
