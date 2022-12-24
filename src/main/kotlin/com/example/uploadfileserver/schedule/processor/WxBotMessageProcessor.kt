package com.example.uploadfileserver.schedule.processor

import com.example.uploadfileserver.eLog
import com.example.uploadfileserver.http.HttpMethod
import com.example.uploadfileserver.iLog
import com.example.uploadfileserver.schedule.AbstractOkCallback
import com.example.uploadfileserver.schedule.MonitorConfig
import com.google.gson.Gson
import java.text.MessageFormat

/**
 * 企业微信群机器人消息处理
 * @author dingpeihua
 * @date 2022/12/21 15:49
 * @version 1.0
 */
class WxBotMessageProcessor : AbsSendMessageProcessor() {
    init {
        val MESSAGE_TEMPLATE = "{0}接口错误率{1}，错误信息:{2}，涉及接口{3}，可能线上生产环境已出现故障,请立即处理！"

    }
    override fun onSendMessage(
        project:String,
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
        sendBotMessage(monitorConfig,content)
    }

   private fun sendBotMessage(monitorConfig: MonitorConfig, message: String) {
        if (monitorConfig.botUrl.isEmpty() || !monitorConfig.isSendMessage) {   //没有配置botUrl，不发送
            eLog {"MonitorScheduledTasks>>" + if (monitorConfig.botUrl.isEmpty()) "企业微信机器人地址未配置!" else "当前是Debug"}
            return
        }
        val params = mutableMapOf<String, Any>()
        params["msgtype"] = "text"
        val params2 = mutableMapOf<String, Any>()
        params2["content"] = "$message\r\n\r\n（消息由BOT机器人自动发出）"
        params2["mentioned_list"] = arrayOf("@all")
        params["text"] = params2
        println(Gson().toJson(params))
       httpPoxy.sendRequest(HttpMethod.POST, monitorConfig.botUrl, Gson().toJson(params), object : AbstractOkCallback<String>() {
           override fun onSuccess(response: String?) {
               iLog { "MonitorScheduledTasks>>发送bot消息成功：$response" }
           }

           override fun onFailure(e: Throwable?) {
               eLog(e) {"MonitorScheduledTasks>>发送bot消息失败：${e?.message}"}
           }
       })
    }
}