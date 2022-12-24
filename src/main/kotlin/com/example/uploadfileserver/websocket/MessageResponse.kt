package com.example.uploadfileserver.websocket

import com.example.uploadfileserver.toJson
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus

/**
 * REST API 返回结果
 *
 * @param <T> 结果集
</T> */
data class MessageResponse<T>(
    private var request: Any? = null,
    var code: Int = 0,
    var timestamp: Long = 0,
    /**
     * 业务错误码
     */
    @JsonProperty("statusCode")
    private var statusCode: Long = 0,
    /**
     * 结果集
     */
    @JsonProperty("data")
    private var data: T? = null
) {

    /**
     * 描述
     */
    @JsonProperty("msg")
    private var msg: String? = null
    fun wasOk(): Boolean {
        return HttpStatus.OK.value().toLong() == statusCode
    }

    /**
     * 服务间调用非业务正常，异常直接释放
     */
    fun serviceData(): T? {
        if (!wasOk()) {
            throw RuntimeException(msg)
        }
        return data
    }

    fun setCode(code: Long): MessageResponse<T> {
        statusCode = code
        return this
    }

    fun setCode(httpStatus: HttpStatus): MessageResponse<T> {
        statusCode = httpStatus.value().toLong()
        msg = httpStatus.reasonPhrase
        return this
    }

    fun setData(data: T): MessageResponse<T> {
        this.data = data
        return this
    }

    fun setMsg(msg: String?): MessageResponse<T> {
        this.msg = msg
        return this
    }

    fun setCode(code: Int): MessageResponse<T> {
        this.code = code
        return this
    }

    fun setTimestamp(timestamp: Long): MessageResponse<T> {
        this.timestamp = timestamp
        return this
    }

    fun getRequest(): Any? {
        return request
    }

    fun setRequest(request: Any?) {
        this.request = request
    }

    override fun toString(): String {
        return this.toJson()
    }

    companion object {
        fun <T> success(data: T, request: Any?): MessageResponse<T> {
            var aec = HttpStatus.OK
            if (data is Boolean && java.lang.Boolean.FALSE == data) {
                aec = HttpStatus.NOT_FOUND
            }
            return restResult(data, request, aec)
        }

        fun <T> msg(msg: String?): MessageResponse<T> {
            val apiResult = MessageResponse<T>()
            apiResult.setMsg(msg)
            return apiResult
        }

        fun <T> msg(code: Int, msg: String?): MessageResponse<T> {
            val apiResult = MessageResponse<T>()
            apiResult.setMsg(msg)
            apiResult.setCode(code)
            return apiResult
        }

        fun <T> restResult(data: T, request: Any?, errorCode: HttpStatus): MessageResponse<T> {
            return restResult(data, request, errorCode.value().toLong(), errorCode.reasonPhrase)
        }

        private fun <T> restResult(
            data: T, request: Any?, code: Long,
            msg: String
        ): MessageResponse<T> {
            val apiResult = MessageResponse<T>()
            apiResult.setCode(code)
            apiResult.setData(data)
            apiResult.request = request
            apiResult.setMsg(if (code == HttpStatus.OK.value().toLong()) "Success" else msg)
            return apiResult
        }
    }
}