package com.example.uploadfileserver.schedule

import com.aliyun.dysmsapi20170525.Client
import com.aliyun.dysmsapi20170525.models.SendSmsRequest
import com.aliyun.tea.TeaModel
import com.aliyun.teaopenapi.models.Config
import com.aliyun.teautil.Common

/**
 * 阿里云短信服务
 * @author dingpeihua
 * @date 2022/4/24 17:45
 * @version 1.0
 */
object AliyunSmsClient {

    /**
     * 使用AK&SK初始化账号Client
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    @Throws(Exception::class)
    fun createClient(accessKeyId: String?, accessKeySecret: String?): Client {
        val config: Config = Config() // 您的AccessKey ID
            .setAccessKeyId(accessKeyId) // 您的AccessKey Secret
            .setAccessKeySecret(accessKeySecret)
        // 访问的域名
        config.endpoint = "dysmsapi.aliyuncs.com"
        return Client(config)
    }

    /**
     * 通过阿里云群发短信功能通知监控异常信息
     * @param phoneNumbers 电话号码列表，以英文逗号分隔
     * @param params [SmsTemplateParams]
     * @author dingpeihua
     * @date 2022/4/25 16:34
     * @version 1.0
     */
    fun sendMessage(phoneNumbers: String, params: SmsTemplateParams) {
        if (params.accessKeyId.isEmpty() || params.accessKeySecret.isEmpty()) {
            throw RuntimeException("accessKeyId or accessKeySecret is empty.")
        }
        if (params.signName.isEmpty() || params.templateCode.isEmpty()) {
            throw RuntimeException("signName or templateCode is empty.")
        }
        //可能线上生产环境已出现故障,请立即处理！
        val client: Client = createClient(params.accessKeyId, params.accessKeySecret)
        val sendSmsRequest = SendSmsRequest()
        sendSmsRequest.setPhoneNumbers(phoneNumbers)
        sendSmsRequest.setSignName(params.signName)
        sendSmsRequest.setTemplateCode(params.templateCode)
        sendSmsRequest.setTemplateParam(params.toString())
        val resp = client.sendSms(sendSmsRequest)
        println(Common.toJSONString(TeaModel.buildMap(resp)))
    }
}

data class SmsTemplateParams(
    /**
     * 阿里云访问密钥ID
     */
    val accessKeyId: String,
    /**
     * 阿里云访问密钥
     */
    val accessKeySecret: String,
    /**
     * 短信模板
     */
    val templateCode: String,
    /**
     * 短信签名
     */
    val signName: String,
    /**
     * 短信模板参数项目名称
     */
    val projectName: String,
    /**
     * 短信模板参数错误率
     */
    val errorRate: String,
    /**
     * 短信模板参数错误信息
     */
    val errorMsg: String,
    /**
     * 短信模板参数涉及接口
     */
    val plat: String
) {
    override fun toString(): String {
        return "{'projectName':'$projectName','errorRate':'$errorRate','errorMsg':'$errorMsg','plat':'$plat'}"
    }
}