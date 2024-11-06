package com.ybzl.mqttmessage.bean


//{
//    "data": {
//        "action": [
//            {
//                "actionType": "airSwitch",  //控制类型
//                "actionValue": "1" //指标值
//            }
//        ],
//        "device": {
//            "id": "49e77e4a56d791671ff0dd166268b322", //设备id
//            "etId": "1809040370418855936", //设备类型
//            "typeName": "空调", //类型名称
//            "ecode": "ZX_YY_F04_AHU405", //设备编码
//            "ename": "空调2-AHU405", //设备名称
//            "projectId": "20231130100002", //企业id
//            "equIp": "192.168.1.1", //ip
//            "roomId": "1" //手术间id
//        }
//    },
//    "topic": "screen/setControl"
//}

open class BaseMessage<T> : Event {
    var data: T? = null
    var topic: String = ""
}

class ActionMessage : BaseMessage<ActionData>()

class ActionData {
    var action: List<Action> = emptyList()
    var device: Device? = null
}

class Action {
    var actionType: String = ""
    var actionValue: String = ""
}

class Device {
    var id: String? = "" //设备id
    var etId: String? = "" //设备类型
    var typeName: String? = "" //类型名称
    var ecode: String? = "" //设备编码
    var ename: String? = "" //设备名称
    var projectId: String? = "" //企业id
    var equIp: String? = "" //ip
    var roomId: String? = "" //手术间id
}

//大屏亮息屏
//	screenIsOn 0-息屏 1-亮屏
//空调
//	airTemperature 温度  数值
//	airHumidity  湿度  数值
//	airSwitch  空调开关 0-关 1-开
//	airDutySwitch  值班开关 0-关 1-开
//	airPlusOrMinusSwitch  正压运行状态开关 0-关（正压） 1-开启
//	airDisinfectSwitch 消毒开关  0-关闭  1-开启
//
//照明
//	lightBrightness 亮度  数值
//	lightColor 色温  数值
//蓝光
//	blueBrightness  亮度  数值
//	blueColor 色温  数值
object ActionType {
    const val SCREEN_IS_ON = "screenIsOn" //大屏亮息屏
    const val AIR_TEMPERATURE = "airTemperature"
    const val AIR_HUMIDITY = "airHumidity"
    const val AIR_SWITCH = "airSwitch"
    const val AIR_DUTY_SWITCH = "airDutySwitch"
    const val AIR_PLUS_OR_MINUS_SWITCH = "airPlusOrMinusSwitch"
    const val AIR_DISINFECT_SWITCH = "airDisinfectSwitch"
    const val LIGHT_BRIGHTNESS = "lightBrightness"
    const val LIGHT_COLOR = "lightColor"
    const val BLUE_BRIGHTNESS = "blueBrightness"
    const val BLUE_COLOR = "blueColor"
}