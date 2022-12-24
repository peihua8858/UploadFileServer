package com.example.uploadfileserver.schedule

import com.example.uploadfileserver.schedule.processor.AlibabaSmsMessageProcessor
import com.example.uploadfileserver.schedule.processor.WxBotMessageProcessor
import com.example.uploadfileserver.schedule.processor.WxSmsMessageProcessor
import com.example.uploadfileserver.websocket.LogWebSocket
import com.example.uploadfileserver.websocket.MessageResponse
import java.text.MessageFormat

/**
 * 发送消息
 * @author dingpeihua
 * @date 2022/12/21 15:43
 * @version 1.0
 */
interface ISendMessage {
    /**
     * 消息发送
     * @param monitorConfig
     * @param projectName 短信模板参数项目名称
     * @param errorRate 短信模板参数错误率
     * @param errorMsg 短信模板参数错误信息
     * @param apiDesc 接口描述信息
     * @param plat 短信模板参数涉及接口
     * @author dingpeihua
     * @date 2022/12/21 15:55
     * @version 1.0
     */
    fun onSendMessage(
        project: String,
        monitorConfig: MonitorConfig,
        title: String,
        errorRate: String,
        errorMsg: String,
        apiDesc: String,
        plat: String
    )

    companion object {
        private val processors = mutableListOf<ISendMessage>()

        init {
            processors.add(AlibabaSmsMessageProcessor())
            processors.add(WxBotMessageProcessor())
            processors.add(WxSmsMessageProcessor())
        }

        @JvmStatic
        fun postMessage(
            project: String,
            monitorConfig: MonitorConfig,
            title: String,
            errorRate: String,
            errorMsg: String,
            apiDesc: String,
            plat: String
        ) {
            val message = MessageFormat.format(
                monitorConfig.msgTemplate,
                "",
                errorRate, errorMsg,
                apiDesc
            )
            LogWebSocket.sendMessage(MessageResponse.msg<String>(1,message))
            for (processor in processors) {
                processor.onSendMessage(project,monitorConfig, title, errorRate, errorMsg, apiDesc, plat)
            }
        }
    }
}