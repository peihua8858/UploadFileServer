package com.example.uploadfileserver

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.Headers
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.model.S3ObjectInputStream
import com.amazonaws.util.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat


@Service
class UploadObjectServer @Autowired constructor(private val amazonS3: AmazonS3) {
    fun uploadFile(
        fileName: String?,
        inputStream: InputStream
    ): URL {
        return upload("img.longxl2021.com/notes", fileName, null, inputStream)
    }

    fun uploadFile(
        fileName: String?,
        inputStream: InputStream,
        optionalMetaData: Map<String, String>?,
    ): URL {
        return upload("img.longxl2021.com/notes", fileName, optionalMetaData, inputStream)
    }

    fun upload(
        bucketName: String,
        fileName: String?,
        optionalMetaData: Map<String, String>?,
        inputStream: InputStream
    ): URL {
        val metadata = ObjectMetadata()
        optionalMetaData?.forEach { (t, u) ->
            metadata.addUserMetadata(t, u)
        }
        try {
            val finalFileName = createFileName(fileName)
            metadata.contentType = optionalMetaData?.get(Headers.CONTENT_TYPE) ?: "image/jpeg"
            val result = amazonS3.putObject(bucketName, finalFileName, inputStream, metadata)
            val s3Url: URL = amazonS3.getUrl(bucketName, finalFileName)
            println("s3Url->$s3Url")
            return s3Url
        } catch (e: AmazonServiceException) {
            throw IllegalStateException("Failed to upload the file", e)
        }
    }

    private val sf = SimpleDateFormat("yyyyMMdd_HHmmssSS")

    /**
     * 根据时间戳创建文件名
     *
     * @return
     */
    fun String.createFileName(extension: String): String {
        val millis = System.currentTimeMillis()
        return this + sf.format(millis) + "." + extension
    }

    fun createFileName(oriFileName: String?): String {
        if (oriFileName.isNullOrEmpty()) {
            return "IMG_".createFileName("jpeg")
        }
        val extension = oriFileName.substringAfterLast('.', "")
        val fileName = oriFileName.substringBeforeLast(".")
        return "IMG_" + fileName.createFileName(extension)
    }

    fun download(path: String?, key: String?): ByteArray? {
        return try {
            val `object`: S3Object = amazonS3.getObject(path, key)
            val objectContent: S3ObjectInputStream = `object`.objectContent
            IOUtils.toByteArray(objectContent)
        } catch (e: AmazonServiceException) {
            throw IllegalStateException("Failed to download the file", e)
        } catch (e: IOException) {
            throw IllegalStateException("Failed to download the file", e)
        }
    }
}