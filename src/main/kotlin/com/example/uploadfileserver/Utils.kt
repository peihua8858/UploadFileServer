package com.example.uploadfileserver

import org.springframework.beans.factory.config.YamlMapFactoryBean
import org.springframework.boot.system.ApplicationHome
import org.springframework.core.io.FileSystemResource
import java.io.File
import java.io.FileNotFoundException

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
        return yamlMapFactoryBean.getObject()?: mutableMapOf()
    }
}