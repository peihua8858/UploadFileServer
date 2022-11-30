package com.example.uploadfileserver.http

import okhttp3.OkHttpClient
import okhttp3.Response

/**
 * Http 请求代理
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/7/1 15:20
 */
interface IOkHttpProxy {
    fun sendRequest(method: HttpMethod, url: String, request: Any): Response
    fun sendRequest(method: HttpMethod, url: String, request: Any, headers: Map<String, String>?): Response
    fun <T> sendRequest(method: HttpMethod, url: String, request: Any, callback: OkCallback<T>)
    fun <T> sendRequest(method: HttpMethod, url: String, request: Any, headers: Map<String, String>?, callback: OkCallback<T>)

    companion object {
        @JvmStatic
        fun getInstance(): IOkHttpProxy {
            return getInstance(OkHttpClient())
        }

        @JvmStatic
        fun getInstance(okHttpClient: OkHttpClient): IOkHttpProxy {
            return OkHttpProxy(okHttpClient)
        }
    }
}