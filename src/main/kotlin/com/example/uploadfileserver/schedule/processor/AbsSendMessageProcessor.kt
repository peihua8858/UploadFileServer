package com.example.uploadfileserver.schedule.processor

import com.example.uploadfileserver.http.OkHttpProxy
import com.example.uploadfileserver.schedule.ISendMessage
import okhttp3.OkHttpClient

abstract class AbsSendMessageProcessor : ISendMessage {
    private val manager = OkHttpProxy.getX509TrustManager()
    protected val httpPoxy by lazy {
        OkHttpProxy(OkHttpClient.Builder()
            .hostnameVerifier { _, _ -> true }
            .sslSocketFactory(OkHttpProxy.getSslSocketFactory(manager), manager)
            .build())

    }
    abstract val serverType: String
    protected fun isSendData(type: String): Boolean {
        return type.isEmpty() || type.contains(serverType)
    }
}