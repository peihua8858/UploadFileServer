package com.example.uploadfileserver.schedule.processor

import com.example.uploadfileserver.eLog
import com.example.uploadfileserver.http.HttpMethod
import com.example.uploadfileserver.iLog
import com.example.uploadfileserver.messageFormat
import com.example.uploadfileserver.schedule.AbstractOkCallback
import com.example.uploadfileserver.schedule.MonitorConfig
import com.fz.common.collections.splicing
import com.google.gson.Gson
import java.text.MessageFormat

/**
 * 企业微信消息通知处理器
 * @author dingpeihua
 * @date 2022/12/21 15:49
 * @version 1.0
 */
class WxSmsMessageProcessor : AbsSendMessageProcessor() {
    override val serverType: String
        get() = "2"

    override fun onSendMessage(
        project: String,
        monitorConfig: MonitorConfig,
        title: String,
        errorRate: String,
        errorMsg: String,
        apiDesc: String,
        plat: String
    ) {
        //"{0}接口错误率{1}，错误信息:{2}，涉及接口{3}，可能线上生产环境已出现故障,请立即处理！"
        val message = MessageFormat.format(
            monitorConfig.msgTemplate,
            "",
            errorRate, errorMsg,
            apiDesc
        )
        sendMessage(monitorConfig, project, title, message)
    }

    override fun onPushMessage(
        sendType: String,
        key: String,
        project: String,
        monitorConfig: MonitorConfig,
        messageData: Any
    ) {
        if (isSendData(sendType)) {
            //"{0}接口错误率{1}，错误信息:{2}，涉及接口{3}，可能线上生产环境已出现故障,请立即处理！"
            val msgTemplate = monitorConfig.msgTemplates[key] ?: return
            val msgContent = msgTemplate.content
            val msgTitle = msgTemplate.title
            val message = msgContent.messageFormat(messageData)
            val title = msgTitle.messageFormat(messageData)
            sendMessage(monitorConfig, project, title, message)
        }
    }

    private fun sendMessage(
        monitorConfig: MonitorConfig,
        project: String,
        title: String,
        message: String
    ) {
        val msgCenter = monitorConfig.msgCenter
        val jobNumbers = monitorConfig.phoneNumbers[project]
        if (msgCenter.smsUrl.isEmpty() || jobNumbers.isNullOrEmpty() || !monitorConfig.isSendMessage) {
            eLog {
                "MonitorScheduledTasks>>" + if (msgCenter.smsUrl.isEmpty()) "消息中心地址未配置!" else if (jobNumbers.isNullOrEmpty()) {
                    "联系人未配置"
                } else "当前是Debug"
            }
            return
        }
        //"{0}接口错误率{1}，错误信息:{2}，涉及接口{3}，可能线上生产环境已出现故障,请立即处理！"
        val data = mutableMapOf<String, String>()
        data["channel"] = "vv"
        data["to"] = jobNumbers.splicing(",") { it.jobNumber.ifEmpty { null } }
        //        data["to"] = "602028,610672"
        data["title"] = title
        data["content"] = message
        val datas = mutableListOf<MutableMap<String, String>>()
        datas.add(data)
        val params = mutableMapOf<String, Any>()
        params["account"] = msgCenter.account
        params["password"] = msgCenter.password
        params["api_key"] = msgCenter.apiKey
        params["data"] = datas
        val result = Gson().toJson(params)
        println(result)
        httpPoxy.sendRequest(HttpMethod.POST, msgCenter.smsUrl, result, object : AbstractOkCallback<String>() {
            override fun onSuccess(response: String?) {
                iLog { "MonitorScheduledTasks>>发送企业微信Sms消息成功：$response" }
            }

            override fun onFailure(e: Throwable?) {
                eLog(e) { "MonitorScheduledTasks>>发送企业微信Sms消息失败：${e?.message}" }
            }
        })
    }
}