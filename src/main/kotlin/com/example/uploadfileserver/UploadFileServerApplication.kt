package com.example.uploadfileserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.system.ApplicationHome
import org.springframework.scheduling.annotation.EnableScheduling
import java.io.File

@EnableScheduling
@SpringBootApplication
class UploadFileServerApplication {
    companion object {
        private const val ROOT_FILE = "data"

        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<UploadFileServerApplication>(*args)
        }

        @JvmStatic
        fun Class<*>.readJarFolder(): File {
            val h = ApplicationHome(this)
            val jarF = h.source
            return jarF.parentFile
        }

        @JvmStatic
        fun Any.readJarFolder(): File {
            return javaClass.readJarFolder()
        }

        @JvmStatic
        fun Any.readRootFileFolder(): File {
            val file = File(readJarFolder(), ROOT_FILE)
            if (!file.exists()) {
                file.mkdirs()
            }
            return file
        }

        @JvmStatic
        fun Any.readFileFolder(folderName: String): File {
            val file = File(readRootFileFolder(), folderName)
            if (!file.exists()) {
                file.mkdirs()
            }
            return file
        }

        @JvmStatic
        fun Any.readFile(fileName: String): File {
            return File(readRootFileFolder(), fileName)
        }
    }
}


