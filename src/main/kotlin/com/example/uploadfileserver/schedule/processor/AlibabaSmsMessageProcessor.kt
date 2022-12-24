package com.example.uploadfileserver.schedule.processor

import com.example.uploadfileserver.eLog
import com.example.uploadfileserver.schedule.AliyunSmsClient
import com.example.uploadfileserver.schedule.MonitorConfig
import com.example.uploadfileserver.schedule.SmsTemplateParams
import com.fz.common.map.deepClone

/**
 * 阿里巴巴 短信发送器
 * @author dingpeihua
 * @date 2022/12/21 15:49
 * @version 1.0
 */
class AlibabaSmsMessageProcessor : AbsSendMessageProcessor() {
    override fun onSendMessage(
        project: String,
        monitorConfig: MonitorConfig,
        title: String,
        errorRate: String,
        errorMsg: String,
        apiDesc: String,
        plat: String
    ) {
        //发送短信
        sendSmsMessage(
            monitorConfig, project, SmsTemplateParams(
                monitorConfig.accessKeyId,
                monitorConfig.accessKeySecret,
                monitorConfig.templateCode,
                monitorConfig.signName,
                title,
                errorRate,
                errorMsg,
                plat
            )
        )
    }

    private fun sendSmsMessage(monitorConfig: MonitorConfig, project: String, message: SmsTemplateParams) {
        val phoneNumbers = monitorConfig.phoneNumbers.deepClone()
        if (phoneNumbers.isNullOrEmpty() || !monitorConfig.isSendMessage) {
            eLog {"MonitorScheduledTasks>>" + if (phoneNumbers.isNullOrEmpty()) "未设置联系人列表为空!" else "当前是Debug"}
            return
        }
        val result = phoneNumbers[project]
        if (result.isNullOrEmpty()) {
            eLog {"MonitorScheduledTasks>>项目：$project 联系人列表为空!"}
            return
        }
        try {
            AliyunSmsClient.sendMessage(result.joinToString(",") { it.phoneNumber }, message)
        } catch (e: Exception) {
            eLog(e) {"MonitorScheduledTasks>>发送短信失败：${e.message}"}
        }
    }
}