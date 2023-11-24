package com.example.uploadfileserver.schedule.processor

import com.example.uploadfileserver.SiteMessageData
import com.example.uploadfileserver.eLog
import com.example.uploadfileserver.schedule.AliyunSmsClient
import com.example.uploadfileserver.schedule.MonitorConfig
import com.example.uploadfileserver.schedule.PhoneNumber
import com.example.uploadfileserver.schedule.SmsTemplateParams
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap

/**
 * 阿里巴巴 短信发送器
 * @author dingpeihua
 * @date 2022/12/21 15:49
 * @version 1.0
 */
class AlibabaSmsMessageProcessor : AbsSendMessageProcessor() {
    override val serverType: String
        get() = "1"

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
            serverType,
            monitorConfig, project, SmsTemplateParams(
                monitorConfig.accessKeyId,
                monitorConfig.accessKeySecret,
                monitorConfig.templateCode,
                monitorConfig.signName,
            ), SiteMessageData(title, errorRate, errorMsg, plat)
        )
    }

    override fun onPushMessage(
        sendType: String,
        key: String,
        project: String,
        monitorConfig: MonitorConfig,
        messageData: Any
    ) {
        try {
            val templateCode = monitorConfig.templateCodes[key]
            if (templateCode.isNullOrEmpty()) {
                return
            }
            //发送短信
            sendSmsMessage(
                sendType,
                monitorConfig,
                project,
                SmsTemplateParams(
                    monitorConfig.accessKeyId,
                    monitorConfig.accessKeySecret,
                    templateCode,
                    monitorConfig.signName
                ),
                messageData
            )
        } catch (e: Throwable) {
            e.printStackTrace()
            eLog {"MonitorScheduledTasks>>"+e.stackTraceToString()}
        }
    }

    private fun sendSmsMessage(
        sendType: String,
        monitorConfig: MonitorConfig,
        project: String,
        params: SmsTemplateParams,
        message: Any
    ) {
        if (isSendData(sendType)) {
            val phoneNumbers = try {
                monitorConfig.phoneNumbers.toMutableMap()
            } catch (e: Exception) {
                eLog {"MonitorScheduledTasks>>"+e.stackTraceToString()}
                monitorConfig.phoneNumbers
            }
            if (phoneNumbers.isNullOrEmpty() || !monitorConfig.isSendMessage) {
                eLog { "MonitorScheduledTasks>>" + if (phoneNumbers.isNullOrEmpty()) "未设置联系人列表为空!" else "当前是Debug" }
                return
            }
            val ignoreCasePhoneNumbers = CaseInsensitiveKeyMap<MutableList<PhoneNumber>>()
            ignoreCasePhoneNumbers.putAll(phoneNumbers)
            val result = ignoreCasePhoneNumbers[project]
            if (result.isNullOrEmpty()) {
                eLog { "MonitorScheduledTasks>>项目：$project 联系人列表为空!" }
                return
            }
            try {
                AliyunSmsClient.sendMessage(result.joinToString(",") { it.phoneNumber }, params, message)
            } catch (e: Exception) {
                eLog(e) { "MonitorScheduledTasks>>发送短信失败：${e.message}" }
            }
        }
    }
}