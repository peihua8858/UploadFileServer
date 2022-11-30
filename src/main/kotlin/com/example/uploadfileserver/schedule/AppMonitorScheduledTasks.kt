package com.example.uploadfileserver.schedule

import com.example.uploadfileserver.UploadFileServerApplication.Companion.readFile
import com.example.uploadfileserver.Utils
import com.fz.common.array.isNonEmpty
import com.fz.common.map.copyOfMapList
import com.fz.common.utils.toDouble
import com.google.gson.reflect.TypeToken
import com.peihua8858.GsonFactory
import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import kotlin.concurrent.thread


/**
 * 网站监控定时任务配置
 *
 * @author dingpeihua
 * @date 2022/4/20 10:29
 * @version 1.0
 */
@Component
class AppMonitorScheduledTasks {
    private var failRate: Double = 0.6
    private var failRateMap: MutableMap<String, Double> = mutableMapOf()
    private var botUrl: String = ""

    /**
     * 企业微信消息模板
     */
    private var messageTemplate: String = ""
    private var templateCode: String = ""
    private var signName: String = ""
    private var accessKeyId: String = ""
    private var accessKeySecret: String = ""
    private val lock: Any = Any()
    private val jsonData = mutableMapOf<String, MutableMap<String, MutableList<MonitorData>>>()

    private val phoneNumbers = mutableMapOf<String, MutableList<PhoneNumber>>()
    private var loadingJsonData = false
    private val phoneNumberToken = object : TypeToken<Map<String, MutableList<PhoneNumber>>>() {
    }
    private val jsonDataToken = object : TypeToken<Map<String, MutableList<MonitorData>>>() {
    }
    private val poolExecutor = ScheduledThreadPoolExecutor(5)

    companion object {
        private  val LOG = LoggerFactory.getLogger(AppMonitorScheduledTasks::class.java)
        const val MESSAGE_TEMPLATE = "{0}接口错误率{1}，错误信息:{2}，涉及接口{3}，可能线上生产环境已出现故障,请立即处理！"
        private const val MONITOR_FILE = "monitor"
    }

    fun runInvalidJsonData(callback: (MutableMap<String, MutableMap<String, MutableList<MonitorData>>>) -> Unit) {
        synchronized(lock) {
            if (loadingJsonData) {
                return
            }
            loadingJsonData = true
            jsonData.clear()
            phoneNumbers.clear()
            phoneNumbers.putAll(parseJson("phone_numbers_data", phoneNumberToken))
            val result = readConfig()
            failRate = result["fail_rate"].toDouble()
            failRateMap = result["fail_rates"] as MutableMap<String, Double>
            LOG.info("MonitorScheduledTasks>>读取阈值配置：${failRate}")
            botUrl = result["bot_url"].toString()
            LOG.info("MonitorScheduledTasks>>读取机器人配置：${botUrl}")
            messageTemplate = result["msg_template"].toString()
            messageTemplate = messageTemplate.ifEmpty { MESSAGE_TEMPLATE }
            LOG.info("MonitorScheduledTasks>>读取模板配置：${messageTemplate}")
            templateCode = result["template_code"].toString()
            signName = result["sign_name"].toString()
            accessKeyId = result["access_key_id"].toString()
            accessKeySecret = result["access_key_secret"].toString()
            if (failRate <= 0.0) {
                failRate = 0.6
            }
            parseJsonData()
            loadingJsonData = false
            callback(jsonData)
        }
    }

    private fun parseJsonData() {
        val jsonFile = readFile("$MONITOR_FILE/api")
        val files = jsonFile.listFiles()
        val jsonData = mutableMapOf<String, MutableMap<String, MutableList<MonitorData>>>()
        if (files.isNonEmpty()) {
            for (file in files) {
                val result = parseJson(file, jsonDataToken)
                jsonData[file.nameWithoutExtension] = result
            }
        }
        this.jsonData.putAll(jsonData)
    }

    private fun readConfig(): Map<String, Any> {
        val map = Utils.readYml(readFile("$MONITOR_FILE/config.yml").absolutePath)
        LOG.info("MonitorScheduledTasks>>读取配置文件成功：{0}", map)
        return map
    }

    /**
     * 解析json数据文件
     * @param jsonName
     * @author dingpeihua
     * @date 2022/4/24 17:52
     * @version 1.0
     */
    private fun <T> parseJson(
        jsonName: String,
        rawType: TypeToken<Map<String, MutableList<T>>>
    ): MutableMap<String, MutableList<T>> {
        try {
            val jsonFile = readFile("$MONITOR_FILE/$jsonName.json")
            return parseJson(jsonFile, rawType)
        } catch (e: Exception) {
            LOG.info("MonitorScheduledTasks>>读取配置文件成功:${e.message}")
        }
        return mutableMapOf()
    }

    /**
     * 解析json数据文件
     * @param jsonName
     * @author dingpeihua
     * @date 2022/4/24 17:52
     * @version 1.0
     */
    private fun <T> parseJson(
        jsonFile: File,
        rawType: TypeToken<Map<String, MutableList<T>>>
    ): MutableMap<String, MutableList<T>> {
        try {
            val jsonContent = FileUtils.readFileToString(jsonFile, "utf-8")
            if (jsonContent.isNullOrEmpty()) {
                LOG.info("MonitorScheduledTasks>>读取数据为空。")
                return mutableMapOf()
            }
            val gsonBuilder = GsonFactory.defaultBuilder()
            gsonBuilder.setLenient()
            val datas: MutableMap<String, MutableList<T>>? =
                gsonBuilder.create().fromJson(jsonContent, rawType.type)
            if (datas.isNullOrEmpty()) {
                LOG.info("MonitorScheduledTasks>>解析数据为空。")
                return mutableMapOf()
            }
            return datas
        } catch (e: Exception) {
            LOG.info("MonitorScheduledTasks>>读取配置文件成功:${e.message}")
        }
        return mutableMapOf()
    }

    fun scheduleTask(jsonData: MutableMap<String, MutableMap<String, MutableList<MonitorData>>>) {
        jsonData.forEach { (t, json) ->
            scheduleTask(t, json)
        }
    }

    fun scheduleTask(taskName: String, jsonData: MutableMap<String, MutableList<MonitorData>>) {
        poolExecutor.execute {
            val tempJsonData = jsonData
            LOG.info("MonitorScheduledTasks>>start task $taskName")
            val data = tempJsonData.copyOfMapList()
            runTaskProcessor(data)
            LOG.info("MonitorScheduledTasks>>start task $taskName")
        }
    }

    fun runTaskProcessor(data: Map<String, MutableList<MonitorData>>) = thread {
        LOG.info("MonitorScheduledTasks>>job start")
        TaskProcessor(
            failRate,
            messageTemplate,
            botUrl,
            failRateMap,
            templateCode,
            signName,
            accessKeyId,
            accessKeySecret,
            phoneNumbers
        ).runDataProcess(data)
        LOG.info("MonitorScheduledTasks>>job Done>>>")
    }

    /**
     * 每12分钟刷新一下配置文件
     * [秒] [分] [小时] [日] [月] [周] [年]
     */
    @Scheduled(cron = "0 */12 * ? * *")
    fun runInvalidJsonData() {
        runInvalidJsonData {
            LOG.info("MonitorScheduledTasks>>读取配置文件成功")
        }
    }

    /**
     * 网络监测任务
     * 每5分钟执行一次检查系统是否正常
     * [秒] [分] [小时] [日] [月] [周] [年]
     */
//    @Scheduled(cron = "*/2 * * * * ?")
//    @Scheduled(cron = "0/3 * * * * *")
    @Scheduled(cron = "0 */5 * ? * *")
    fun runMonitorTask() {
        if (jsonData.isEmpty()) {
            runInvalidJsonData {
                LOG.info("MonitorScheduledTasks>>读取配置文件成功")
                scheduleTask(it)
            }
            return
        }
        scheduleTask(jsonData)
    }
}

