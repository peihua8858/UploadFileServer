package com.example.uploadfileserver.schedule.processor

import com.example.uploadfileserver.eLog
import com.example.uploadfileserver.http.HttpMethod
import com.example.uploadfileserver.iLog
import com.example.uploadfileserver.messageFormat
import com.example.uploadfileserver.schedule.AbstractOkCallback
import com.example.uploadfileserver.schedule.MonitorConfig
import com.google.gson.Gson
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap
import java.text.MessageFormat

/**
 * 企业微信群机器人消息处理
 * @author dingpeihua
 * @date 2022/12/21 15:49
 * @version 1.0
 */
class WxBotMessageProcessor : AbsSendMessageProcessor() {
    override val serverType: String
        get() = "3"

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
        val content = MessageFormat.format(
            monitorConfig.msgTemplate,
            title,
            errorRate, errorMsg,
            apiDesc
        )
        sendBotMessage(project,monitorConfig, content)
    }


    override fun onPushMessage(
        sendType: String,
        key: String,
        project: String,
        monitorConfig: MonitorConfig,
        messageData: Any
    ) {
        try {
            if (isSendData(sendType)) {
                //"{0}接口错误率{1}，错误信息:{2}，涉及接口{3}，可能线上生产环境已出现故障,请立即处理！"
                val msgTemplate = monitorConfig.msgTemplates[key] ?: return
                val msgContent = msgTemplate.content
                val content = msgContent.messageFormat(messageData)
                sendBotMessage(project,monitorConfig, content)
            }
        } catch (e: Throwable) {
           e.printStackTrace()
            eLog {"MonitorScheduledTasks>>"+e.stackTraceToString()}
        }
    }

    private fun sendBotMessage(project: String, monitorConfig: MonitorConfig, message: String) {
        val botUrls = monitorConfig.botUrls.toMutableMap()
        if (botUrls.isNullOrEmpty() || !monitorConfig.isSendMessage) {   //没有配置botUrl，不发送
            eLog { "MonitorScheduledTasks>>" + if (botUrls.isNullOrEmpty()) "企业微信机器人地址未配置!" else "当前是Debug" }
            return
        }
        val ignoreCaseBotUrls = CaseInsensitiveKeyMap<String>()
        ignoreCaseBotUrls.putAll(botUrls)
        val botUrl = ignoreCaseBotUrls[project]
        if (botUrl.isNullOrEmpty()) {
            eLog { "MonitorScheduledTasks>>项目：$project 企业微信机器人地址未配置!" }
            return
        }
        val params = mutableMapOf<String, Any>()
        params["msgtype"] = "text"
        val params2 = mutableMapOf<String, Any>()
        params2["content"] = "$message\r\n\r\n（消息由BOT机器人自动发出）"
        params2["mentioned_list"] = arrayOf("@all")
        params["text"] = params2
        println(Gson().toJson(params))
        httpPoxy.sendRequest(
            HttpMethod.POST,
            botUrl,
            Gson().toJson(params),
            object : AbstractOkCallback<String>() {
                override fun onSuccess(response: String?) {
                    iLog { "MonitorScheduledTasks>>发送bot消息成功：$response" }
                }

                override fun onFailure(e: Throwable?) {
                    eLog(e) { "MonitorScheduledTasks>>发送bot消息失败：${e?.message}" }
                }
            })
    }
}