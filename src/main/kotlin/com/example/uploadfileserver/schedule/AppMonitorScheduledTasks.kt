package com.example.uploadfileserver.schedule

import com.example.uploadfileserver.UploadFileServerApplication.Companion.readFile
import com.example.uploadfileserver.Utils
import com.example.uploadfileserver.eLog
import com.example.uploadfileserver.iLog
import com.fz.common.array.isNonEmpty
import com.fz.common.map.copyOfMapList
import com.google.gson.reflect.TypeToken
import com.peihua8858.GsonFactory
import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
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
    private val lock: Any = Any()
    private val jsonData = mutableMapOf<String, MutableMap<String, MutableList<MonitorData>>>()

    private val phoneNumbers = mutableMapOf<String, MutableList<PhoneNumber>>()
    private var loadingJsonData = false
    private val phoneNumberToken = object : TypeToken<Map<String, MutableList<PhoneNumber>>>() {
    }
    private val jsonDataToken = object : TypeToken<Map<String, MutableList<MonitorData>>>() {
    }
    private val poolExecutor = ScheduledThreadPoolExecutor(5)
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
                jsonData.clear()
                phoneNumbers.clear()
                phoneNumbers.putAll(parseJson("phone_numbers_data", phoneNumberToken))
                monitorConfig = readNewConfig()
                monitorConfig.phoneNumbers = phoneNumbers
                parseJsonData()
                loadingJsonData = false
                callback(jsonData)
            } catch (e: Throwable) {
                //异常清除数据，下次重新读取
                phoneNumbers.clear()
                jsonData.clear()
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
        this.jsonData.putAll(jsonData)
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
        jsonData.forEach { (t, json) ->
            scheduleTask(t, json)
        }
    }

    fun scheduleTask(taskName: String, jsonData: MutableMap<String, MutableList<MonitorData>>) {
        poolExecutor.execute {
            val tempJsonData = jsonData
            iLog { "MonitorScheduledTasks>>start task $taskName" }
            val data = tempJsonData.copyOfMapList()
            runTaskProcessor(data)
            iLog { "MonitorScheduledTasks>>end task $taskName" }
        }
    }

    fun runTaskProcessor(data: Map<String, MutableList<MonitorData>>) = thread {
        iLog { "MonitorScheduledTasks>>job start" }
        TaskProcessor(monitorConfig).runDataProcess(data)
        iLog { "MonitorScheduledTasks>>job Done>>>" }
    }

    /**
     * 每12分钟刷新一下配置文件
     * [秒] [分] [小时] [日] [月] [周] [年]
     */
    @Scheduled(cron = "0 */12 * ? * *")
    fun runInvalidJsonData() {
        runInvalidJsonData {
            iLog { "MonitorScheduledTasks>>读取配置文件成功" }
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
        if (jsonData.isEmpty()) {
            runInvalidJsonData {
                iLog { "MonitorScheduledTasks>>读取配置文件成功" }
                scheduleTask(it)
            }
            return
        }
        scheduleTask(jsonData)
    }
}

