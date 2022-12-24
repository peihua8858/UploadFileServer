package com.example.uploadfileserver.schedule

/**
 * 监控配置项
 * @author dingpeihua
 * @date 2022/12/23 14:24
 * @version 1.0
 */
class MonitorConfig(
    val failRate: Double = 0.6,
    val failRates: MutableMap<String, Double> = mutableMapOf(),
    val botUrl: String = "",
    /**
     * 企业微信消息模板
     */
    val msgTemplate: String = "",
    val templateCode: String = "",
    val signName: String = "",
    val accessKeyId: String = "",
    val accessKeySecret: String = "",
    var phoneNumbers: MutableMap<String, MutableList<PhoneNumber>> = mutableMapOf(),
    val msgCenter: MsgCenterConfig = MsgCenterConfig(),
    val isSendMessage: Boolean = true
) {
}