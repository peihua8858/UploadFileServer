package com.example.uploadfileserver

import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MultipartFile

/**
 *  文件上传控制器
 * @author dingpeihua
 * @date 2022/10/26 19:59
 * @version 1.0
 */
@RequestMapping("/upload")
@Controller
class UploadFileController @Autowired constructor(private val uploadObjectServer: UploadObjectServer) {
    @RequestMapping("/file")
    @ResponseBody
    fun uploadFile(@RequestParam("file") file: MultipartFile): String {
        println(">>>>>>>>>>uploadFile:" + file.originalFilename)
        val metadata = mutableMapOf<String, String>()
        metadata["Content-Type"] = file.contentType ?: "image/*"
        metadata["Content-Length"] = file.size.toString()
        val result = uploadObjectServer.uploadFile(file.originalFilename, file.inputStream, metadata)
        val url = result.toString()
        val res = mutableMapOf<String, MutableList<ResultData>>()
        val list = mutableListOf<ResultData>()
        list.add(ResultData(file.originalFilename, file.size, url, file.contentType, url))
        res["files"] = list
        return Gson().toJson(res)
    }

    @RequestMapping("/files",consumes = ["multipart/form-data"])
    @ResponseBody
    fun uploadFiles(@RequestParam("files") files: Array<MultipartFile>): String {
        val result = mutableListOf<ResultData>()
        files.forEach {
            val metadata = mutableMapOf<String, String>()
            metadata["Content-Type"] = it.contentType ?: "image/*"
            metadata["Content-Length"] = it.size.toString()
            val res = uploadObjectServer.uploadFile(it.originalFilename, it.inputStream)
            val url = res.toString()
            result.add(ResultData(it.originalFilename, it.size, url, it.contentType, url))

        }
        val res = mutableMapOf<String, MutableList<ResultData>>()
        res["files"] = result
        return Gson().toJson(res)
    }
}