package com.ybzl.mqttmessage.setting

import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.NetworkUtils.OnNetworkStatusChangedListener

object NetSetting : OnNetworkStatusChangedListener {

    private var ipv4 = ""

    fun initNet() {
        NetworkUtils.registerNetworkStatusChangedListener(this)
    }

    fun padIPv4Address(reservePoint: Boolean = true): String {
        // 将IP地址按"."分割
        val parts = ipv4.split(".")
        if (parts.isEmpty()) {
            return ""
        }
        // 对每个部分进行补零处理
        val paddedParts = parts.map { it.toInt().toString().padStart(3, '0') }
        // 重新组合成完整的IP地址
        return paddedParts.joinToString(if (reservePoint) "." else "")
    }


    override fun onDisconnected() {

    }

    override fun onConnected(networkType: NetworkUtils.NetworkType?) {
        ipv4 = NetworkUtils.getIPAddress(true) ?: ""
    }

}