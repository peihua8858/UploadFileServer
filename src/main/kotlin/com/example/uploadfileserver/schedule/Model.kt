package com.example.uploadfileserver.schedule

import com.example.uploadfileserver.http.HttpMethod
import com.example.uploadfileserver.http.OkCallback
import java.io.Serializable

abstract class AbstractOkCallback<T> : OkCallback<T>
data class MonitorData(
    /**
     * 站点名称
     */
    val siteName: String,
    /**
     * 请求方式
     */
    val method: HttpMethod = HttpMethod.POST,
    /**
     * 请求接口名称
     */
    val apiName: String,
    /**
     * 接口描述信息
     */
    val apiDesc: String,
    /**
     * 请求url地址
     */
    val url: String,
    /**
     * 参数列表
     */
    val params: String,
    /**
     * 请求头列表
     */
    val headers: Map<String, String>,
    /**
     * 是否渗透缓存
     */
    val isPenetrateCache: Boolean = false,
    /**
     * 最小错误码
     */
    val minCode: Int = 500,
    /**
     * 最大错误码
     */
    val maxCode: Int = 599,
    /**
     * 响应数据配置
     */
    val patch: ResponsePathConfig?
) : Serializable

data class ResponsePathConfig(
    /**
     * 请求ID
     */
    val requestId: String,
    /**
     * 匹配数
     */
    val matchedNum: String,
    /**
     * 结果码
     */
    val resultCode: String,
    /**
     * 数据结果集路径
     */
    val resultPatch: String
): Serializable

data class TasksResponse(
    val code: Int,
    val message: String,
    val request: MonitorData,
    val response: ResponseResult? = null
)

data class PhoneNumber(val phoneNumber: String, val name: String,val jobNumber:String) : Serializable
data class ResponseResult(val requestId: String, val resultNum: Int, val resultCode: Int, val message: String)