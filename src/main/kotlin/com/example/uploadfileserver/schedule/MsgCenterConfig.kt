package com.example.uploadfileserver.schedule

/**
 * 消息中心配置
 * @author dingpeihua
 * @date 2022/12/23 14:58
 * @version 1.0
 */
data class MsgCenterConfig(
    val smsUrl: String = "",
    val account: String = "",
    val password: String = "",
    val apiKey: String = ""
)