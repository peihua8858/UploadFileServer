package com.example.uploadfileserver

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.config.YamlMapFactoryBean
import org.springframework.boot.system.ApplicationHome
import org.springframework.core.io.FileSystemResource
import java.io.File
import java.io.FileNotFoundException

private val LOG: Log = LogFactory.getLog(Utils::class.java)
private val objectMapper: ObjectMapper by lazy {
    val objectMapper = ObjectMapper()
    objectMapper.configure(
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
        false
    )
    objectMapper.configure(
        JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER,
        true
    )
    objectMapper.configure(
        JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS,
        true
    )
    objectMapper
}

object Utils {

    @Throws(FileNotFoundException::class)
    fun readFile(fileName: String): File {
        val h = ApplicationHome(Utils::class.java)
        val jarF = h.source
        return File(jarF.parentFile, fileName)
    }

    fun readYml(fileName: String): Map<String, Any> {
        val yamlMapFactoryBean = YamlMapFactoryBean()
        //可以加载多个yml文件
        yamlMapFactoryBean.setResources(FileSystemResource(fileName))
        //通过getObject()方法获取Map对象
        return yamlMapFactoryBean.getObject() ?: mutableMapOf()
    }

    fun <T> readYmlByObject(file: File, clazz: Class<T>): T {
        val mapper = ObjectMapper(YAMLFactory())
        return mapper.readValue(file, clazz)
    }

}

/**
 * Convert JavaBean to json string
 *
 * @param object
 * @return json
 */
fun Any?.toJson(): String {
    try {
        return objectMapper.writeValueAsString(this)
    } catch (e: Exception) {
        LOG.error("Failed to toJson with {0}", e)
    }
    return ""
}