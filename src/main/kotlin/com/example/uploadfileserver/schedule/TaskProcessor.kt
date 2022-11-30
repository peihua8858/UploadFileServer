package com.example.uploadfileserver.schedule

import com.example.uploadfileserver.JSON
import com.example.uploadfileserver.http.CodeException
import com.example.uploadfileserver.http.HttpMethod
import com.example.uploadfileserver.http.OkHttpProxy
import com.fz.common.collections.isNonEmpty
import com.fz.common.map.deepClone
import com.fz.common.map.isNonEmpty
import com.fz.common.utils.toInteger
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Response
import org.slf4j.LoggerFactory
import java.text.MessageFormat
import kotlin.random.Random

class TaskProcessor(
    private val failRate: Double,
    private var messageTemplate: String,
    private var botUrl: String,
    private var failRateMap: MutableMap<String, Double>,
    private var templateCode: String = "",
    private var signName: String = "",
    private var accessKeyId: String = "",
    private var accessKeySecret: String = "",
    private var phoneNumbers: MutableMap<String, MutableList<PhoneNumber>>
) {
    /**
     * 用于调试，避免在调试时发送测试信息
     */
    private var isDebug: Boolean = false
    private var isSendMessage: Boolean = true
    private val manager = OkHttpProxy.getX509TrustManager()
    private val httpPoxy = OkHttpProxy(OkHttpClient.Builder()
        .hostnameVerifier { _, _ -> true }
        .sslSocketFactory(OkHttpProxy.getSslSocketFactory(manager), manager)
        .build())

    companion object {
        private val LOG = LoggerFactory.getLogger(TaskProcessor::class.java)
        const val MESSAGE_TEMPLATE = "{0}接口错误率{1}，错误信息:{2}，涉及接口{3}，可能线上生产环境已出现故障,请立即处理！"
    }

    fun runDataProcess(jsonDatas: Map<String, MutableList<MonitorData>>) {
        if (jsonDatas.isEmpty()) {
            LOG.info("MonitorScheduledTasks>>解析数据为空。")
            return
        }
        jsonDatas.forEach {
            val responses = mutableMapOf<Int, MutableList<TasksResponse>>()
            it.value.forEach { item ->
                sendHttpRequest(responses, item)
            }
            val projectKey = it.key
            val dataSize = jsonDatas[projectKey]?.size ?: 0
            parseResult(projectKey, dataSize, responses)
        }
    }

    private fun parseResult(project: String, dataSize: Int, result: MutableMap<Int, MutableList<TasksResponse>>) {
        val failRate = failRateMap[project] ?: failRate
        result.forEach { itemResponses ->
            val responses = itemResponses.value
            val resultSize = responses.size
            if (dataSize == 0 || resultSize == 0) {
                LOG.info("MonitorScheduledTasks>>解析数据为空。")
                return@forEach
            }
            val apiDescSb = StringBuilder()
            var projectName = ""
            var responseMsg = ""
            apiDescSb.append("(")
            val r = resultSize / dataSize.toDouble()
            val rate = (r * 100f).toInt()
            LOG.info("MonitorScheduledTasks>>监控失败率全局阈值:$failRate")
            LOG.info("MonitorScheduledTasks>>失败数:$resultSize，总数:$dataSize，失败率:$rate%")
            LOG.info("MonitorScheduledTasks>>监控失败率阈值:$failRate")
            if (r >= failRate) {
                responses.forEach { response ->
                    val request = response.request
                    if (projectName.isEmpty()) {
                        projectName = "【${request.siteName}】"
                    }
                    if (responseMsg.isEmpty()) {
                        responseMsg = "${response.code}"
                        val msg = response.message
                        responseMsg += if (msg.isEmpty()) "" else "-$msg"
                    }
                    apiDescSb.append(request.apiDesc).append("，")
                }
                apiDescSb.deleteCharAt(apiDescSb.length - 1)
                apiDescSb.append(")")
                val errorRate = "$rate%($resultSize/$dataSize)"
                val apiDesc = apiDescSb.toString()
                //"{0}接口错误率{1}，错误信息:{2}，涉及接口{3}，可能线上生产环境已出现故障,请立即处理！"
                val content = MessageFormat.format(
                    messageTemplate,
                    projectName,
                    errorRate, responseMsg,
                    apiDesc
                )
                LOG.info("MonitorScheduledTasks>>发送监控信息：$content")
                //发送企业微信信息
                sendBotMessage(botUrl, content)
                //发送短信
                sendSmsMessage(
                    project, SmsTemplateParams(
                        accessKeyId, accessKeySecret, templateCode, signName,
                        projectName, errorRate, responseMsg, "具体查看企业微信"
                    )
                )
            }
        }
    }

    private val codes = arrayOf(
        CodeException(200, "Success"),
        CodeException(500, "Internal Server Error"),
        CodeException(501, "Not Implemented"),
        CodeException(502, "Bad Gateway"),
        CodeException(503, "Service Unavailable"),
        CodeException(504, "Gateway Timeout"),
        CodeException(505, "HTTP Version Not Supported")
    )
    private val codesMap = mutableMapOf<Int, CodeException>()

    init {
        for (code in codes) {
            codesMap[code.code] = code
        }
    }

    fun sendHttpRequest(
        result: MutableMap<Int, MutableList<TasksResponse>>,
        params: MonitorData
    ) {
        val url = addTime(params.url, params.isPenetrateCache)
        LOG.info("MonitorScheduledTasks>>开始请求：$url")
        val response = if (isDebug) {
            val exception = codes[Random.nextInt(codes.size)]
            TasksResponse(exception.code, exception.message ?: "", params)
        } else requestApi(url, params)
        if (response != null) {
            val responseCode = response.code
            var responses = result[responseCode]
            if (responses.isNullOrEmpty()) {
                responses = mutableListOf()
            }
            responses.add(TasksResponse(responseCode, response.message, params))
            result[responseCode] = responses
        }
    }

    private fun requestApi(url: String, params: MonitorData): TasksResponse? {
        val response = requestHttp(url, params)
        LOG.info("MonitorScheduledTasks>>请求结果：code：${response.code},message：${response.message}")
        if (response.isSuccessful) {
            val patch = params.patch
            if (patch != null) {
                val responseContent = response.body?.string()
                val contentMap = JSON.parseObject(responseContent)
                val contentResponse = parseResponse(patch, contentMap) ?: return null
                return TasksResponse(contentResponse.resultCode, contentResponse.message, params, contentResponse)
            }
            return null
        }
        val responseCode = response.code
        if ((responseCode in params.minCode..params.maxCode)) {
            var message = response.message
            if (message.isEmpty()) {
                message = codesMap[responseCode]?.message ?: "Unknown error"
            }
            return TasksResponse(responseCode, message, params)
        }
        return null
    }

    private fun parseResponse(patch: ResponsePathConfig, map: Map<String, Any>): ResponseResult? {
        val resultCodeKey = patch.resultCode
        val requestIdKey = patch.requestId
        val matchNumKey = patch.matchedNum
        val resultPatchKey = patch.resultPatch
        val resultCode = processNodePath(map, resultCodeKey).toInteger(-1)
        val requestId = processNodePath(map, requestIdKey).toString()
        val matchNum = processNodePath(map, matchNumKey).toInteger()
        val result = processNodePath(map, resultPatchKey)
        if ((result is List<*> && result.isNonEmpty()) || (result is Map<*, *> && result.isNonEmpty())) {
            return null
        } else if (result != null) {
            return null
        }
        return ResponseResult(requestId, matchNum, resultCode, "响应数据结果'$resultPatchKey' 为空，请求ID：$requestId")
    }

    private fun requestHttp(url: String, params: MonitorData): Response {
        val headers = params.headers
        if (headers.containsKey("Content-Type")) {
            val contentType = headers["Content-Type"]
            if (contentType.equals("x-www-form-urlencoded", true)) {
                val map = JSON.parseObject(params.params)
                return httpPoxy.sendRequest(params.method, url, map, params.headers)
            }
        }
        return httpPoxy.sendRequest(params.method, url, params.params, params.headers)
    }

    /**
     * 数据解析
     *
     * @param map
     * @param path
     * @return
     */
    private fun processNodePath(map: Map<String, Any>, path: String): Any? {
        var node: Any? = map
        val pah = path.split("\\.".toRegex()).toTypedArray()
        for (p in pah) {
            node = if (p.startsWith("[")) {
                val index = p.substring(1, p.length - 1).toInt()
                (node as? List<Any>?)?.get(index)
            } else {
                (node as? Map<String, Any>?)?.get(p)
            }
        }
        return node
    }

    fun sendSmsMessage(project: String, message: SmsTemplateParams) {
        val phoneNumbers = phoneNumbers.deepClone()
        if (phoneNumbers.isNullOrEmpty() || !isSendMessage) {
            LOG.info("MonitorScheduledTasks>>" + if (phoneNumbers.isNullOrEmpty()) "未设置联系人列表为空!" else "当前是Debug")
            return
        }
        val result = phoneNumbers[project]
        if (result.isNullOrEmpty()) {
            LOG.info("MonitorScheduledTasks>>项目：$project 联系人列表为空!")
            return
        }
        try {
            AliyunSmsClient.sendMessage(result.joinToString(",") { it.phoneNumber }, message)
        } catch (e: Exception) {
            LOG.info("MonitorScheduledTasks>>发送短信失败：${e.message}")
        }
    }

    fun sendBotMessage(botUrl: String, message: String) {
        if (botUrl.isEmpty() || !isSendMessage) {   //没有配置botUrl，不发送
            LOG.info("MonitorScheduledTasks>>" + if (botUrl.isEmpty()) "企业微信机器人地址未配置!" else "当前是Debug")
            return
        }
        val params = mutableMapOf<String, Any>()
        params["msgtype"] = "text"
        val params2 = mutableMapOf<String, Any>()
        params2["content"] = "$message\r\n\r\n（消息由BOT机器人自动发出）"
        params2["mentioned_list"] = arrayOf("@all")
        params["text"] = params2
        println(Gson().toJson(params))
        httpPoxy.sendRequest(HttpMethod.POST, botUrl, Gson().toJson(params), object : AbstractOkCallback<String>() {
            override fun onSuccess(response: String?) {
                LOG.info("MonitorScheduledTasks>>发送bot消息成功：$response")
            }

            override fun onFailure(e: Throwable?) {
                LOG.info("MonitorScheduledTasks>>发送bot消息失败：${e?.message}")
            }
        })
    }

    private fun addTime(url: String, addRealTime: Boolean = false): String {
        if (addRealTime) {
            val time = System.currentTimeMillis()
            val index = url.indexOf("?")
            if (index == -1) {
                return "$url?time=$time"
            }
            return "$url&time=$time"
        }
        return url
    }
}