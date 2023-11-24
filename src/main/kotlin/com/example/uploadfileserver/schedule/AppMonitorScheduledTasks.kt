package com.example.uploadfileserver.schedule

import com.example.uploadfileserver.RmsMessageData
import com.example.uploadfileserver.UploadFileServerApplication.Companion.readFile
import com.example.uploadfileserver.Utils
import com.example.uploadfileserver.eLog
import com.example.uploadfileserver.iLog
import com.fz.common.array.isNonEmpty
import com.google.gson.reflect.TypeToken
import com.peihua8858.GsonFactory
import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import java.util.*


/**
 * 网站监控定时任务配置
 *
 * @author dingpeihua
 * @date 2022/4/20 10:29
 * @version 1.0
 */
@Component
class AppMonitorScheduledTasks {
    private val lock: Any = Any()
    private val mJsonData = mutableMapOf<String, MutableMap<String, MutableList<MonitorData>>>()

    private val phoneNumbers = mutableMapOf<String, MutableList<PhoneNumber>>()
    private var loadingJsonData = false
    private val phoneNumberToken = object : TypeToken<Map<String, MutableList<PhoneNumber>>>() {
    }
    private val jsonDataToken = object : TypeToken<Map<String, MutableList<MonitorData>>>() {
    }
    private var monitorConfig: MonitorConfig = MonitorConfig()

    companion object {
        private const val MONITOR_FILE = "monitor"
    }

    fun runInvalidJsonData(callback: (MutableMap<String, MutableMap<String, MutableList<MonitorData>>>) -> Unit) {
        synchronized(lock) {
            if (loadingJsonData) {
                return
            }
            try {
                loadingJsonData = true
                mJsonData.clear()
                phoneNumbers.clear()
                phoneNumbers.putAll(parseJson("phone_numbers_data", phoneNumberToken))
                monitorConfig = readNewConfig()
                monitorConfig.phoneNumbers = phoneNumbers
                parseJsonData()
                loadingJsonData = false
                callback(mJsonData)
            } catch (e: Throwable) {
                //异常清除数据，下次重新读取
                phoneNumbers.clear()
                mJsonData.clear()
                loadingJsonData = false
                e.printStackTrace()
//                eLog(e) { "MonitorScheduledTasks>>读取配置文件失败。" }
            }
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
        this.mJsonData.putAll(jsonData)
    }

    private fun readNewConfig(): MonitorConfig {
        val result = Utils.readYmlByObject(readFile("$MONITOR_FILE/config.yml"), MonitorConfig::class.java)
        iLog { "MonitorScheduledTasks>>读取配置文件成功：{0}$result" }
        return result
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
            eLog(e) { "MonitorScheduledTasks>>读取配置文件失败:${e.message}" }
//            LOG.error("MonitorScheduledTasks>>读取配置文件成功:${e.message}")
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
                eLog { "MonitorScheduledTasks>>读取数据为空。" }
                return mutableMapOf()
            }
            val gsonBuilder = GsonFactory.defaultBuilder()
            gsonBuilder.setLenient()
            val datas: MutableMap<String, MutableList<T>>? =
                gsonBuilder.create().fromJson(jsonContent, rawType.type)
            if (datas.isNullOrEmpty()) {
                eLog { "MonitorScheduledTasks>>解析数据为空。" }
                return mutableMapOf()
            }
            return datas
        } catch (e: Exception) {
            eLog(e) { "MonitorScheduledTasks>>读取配置文件失败:${e.message}" }
        }
        return mutableMapOf()
    }

    fun scheduleTask(jsonData: MutableMap<String, MutableMap<String, MutableList<MonitorData>>>) {
        try {
            val tempJsonData = try {
                jsonData.toMutableMap()
            } catch (e: Exception) {
                jsonData
            } ?: return
            val ignoreCaseJsonData = CaseInsensitiveKeyMap<MutableMap<String, MutableList<MonitorData>>>()
            ignoreCaseJsonData.putAll(tempJsonData)
            ignoreCaseJsonData.forEach { (t, json) ->
                iLog { "MonitorScheduledTasks>>start task $t" }
                iLog { "MonitorScheduledTasks>>job start" }
                TaskProcessor(monitorConfig).runDataProcess(json)
                iLog { "MonitorScheduledTasks>>job Done>>>" }
                iLog { "MonitorScheduledTasks>>end task $t" }
            }
        } catch (e: Exception) {
           e.printStackTrace()
        }
    }

    /**
     * 每12分钟刷新一下配置文件
     * [秒] [分] [小时] [日] [月] [周] [年]
     */
    @Scheduled(cron = "0 */12 * ? * *")
    fun runInvalidJsonData() {
        try {
            runInvalidJsonData {
                iLog { "MonitorScheduledTasks>>读取配置文件成功" }
            }
        } catch (e: Throwable) {
            iLog { "MonitorScheduledTasks>>读取配置文件异常" + e.stackTraceToString() }
        }
    }

    /**
     * 网络监测任务
     * 每5分钟执行一次检查系统是否正常
     * [秒] [分] [小时] [日] [月] [周] [年]
     */
//    @Scheduled(cron = "*/2 * * * * ?")
//    @Scheduled(cron = "0/10 * * * * *")
    @Scheduled(cron = "0 */5 * ? * *")
    fun runMonitorTask() {
        try {
            if (mJsonData.isEmpty()) {
                runInvalidJsonData {
                    iLog { "MonitorScheduledTasks>>读取配置文件成功" }
                    scheduleTask(it)
                }
                return
            }
            scheduleTask(mJsonData)
        } catch (e: Throwable) {
            iLog { "MonitorScheduledTasks>>读取配置文件异常" + e.stackTraceToString() }
        }
    }

    fun onPushMessage(sendType: String, key: String, project: String, message: RmsMessageData) {
        try {
            if (phoneNumbers.isEmpty()) {
                runInvalidJsonData {
                    iLog { "MonitorScheduledTasks>>读取配置文件成功" }
                    onPushMessageTask(sendType, key, project, message)
                }
                return
            }
            onPushMessageTask(sendType, key, project, message)
        } catch (e: Throwable) {
            iLog { "MonitorScheduledTasks>>读取配置文件异常" + e.stackTraceToString() }
        }
    }

    fun onPushMessageTask(sendType: String, key: String, project: String, message: RmsMessageData) {
        ISendMessage.postMessage(sendType, key, project, monitorConfig, message)
    }
}

