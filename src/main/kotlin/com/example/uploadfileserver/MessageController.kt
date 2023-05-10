package com.example.uploadfileserver

import com.example.uploadfileserver.schedule.AppMonitorScheduledTasks
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

/**
 * 消息推送接口
 */
@Controller
@RequestMapping(*["/message"])
class MessageController constructor(val scheduledTasks: AppMonitorScheduledTasks) {

    /**
     * 消息推送
     * @param message 消息推送接口参数对象
     * @author dingpeihua
     * @date 2023/5/10 13:48
     * @version 1.0
     */
    @PostMapping("/push")
    @ResponseBody
    fun receiveMsg(@RequestBody message: RmsMessageData): String {
        var businessType = message.businessType
        if (businessType.isEmpty()) {
            businessType = "RMS"
        }
        scheduledTasks.onPushMessage(message.sendType, businessType, message.projectName, message)
        return "{" +
                "\"success\": true,\n" +
                "\"message\": \"success\",\n" +
                " \"code\": \"200\"\n" +
                "}";
    }
}