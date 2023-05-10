package com.example.uploadfileserver

/**
 *  消息推送请求参数
 * @author dingpeihua
 * @date 2023/5/10 12:16
 * @version 1.0
 */
data class RmsMessageData(
    /**
     * 固定名项目名称，如：Zaful、Dresslily、Rosegal
     */
    val projectName: String,
    /**
     * 项目名称加平台，如：【Zaful-安卓APP】、【Zaful-iOS APP】
     */
    val projectPlatform: String,
    /**
     * 业务名称，如：【Zaful-安卓APP-下单】
     */
    val businessName: String,
    /**
     * 行为，如：下单行为
     */
    val behavior: String,
    /**
     *  时间，如60分钟
     */
    val time: String,
    /**
     * 检查操作，如：下单操作
     */
    val plat: String,
    /**
     * 发送类型，1表示仅发送短信，2表示仅发送wxSms，3表示仅发送WxBot，多个用英文逗号分隔*
     */
    val sendType: String = "",
    /**
     * 业务类型，目前写,RMS,后续可定制
     */
    val businessType: String = "RMS"
){
    override fun toString(): String {
        return "{'businessName':'$businessName','behavior':'$behavior','time':'$time','plat':'$plat'}"
    }
}