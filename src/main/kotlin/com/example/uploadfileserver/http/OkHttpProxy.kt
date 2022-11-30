package com.example.uploadfileserver.http

import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * OkHttp请求代理
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/6/30 20:17
 */
class OkHttpProxy(val httpClient: OkHttpClient) : IOkHttpProxy {
    override fun sendRequest(method: HttpMethod, url: String, request: Any): Response {
        return sendRequest(method, url, request, mutableMapOf())
    }

    override fun sendRequest(method: HttpMethod, url: String, request: Any, headers: Map<String, String>?): Response {
        val requestBuilder = Request.Builder()
        requestBuilder.url(url)
        if (!headers.isNullOrEmpty()) {
            val headersBuilder = Headers.Builder()
            for ((key, value) in headers) {
                headersBuilder.add(key, value)
            }
            requestBuilder.headers(headersBuilder.build())
        }
        if (request is String) {
            if (method == HttpMethod.POST) {
                requestBuilder.post(request.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            } else {
                requestBuilder.get()
            }
        } else if (request is File) {
            requestBuilder.post(request.asRequestBody("application/octet-stream".toMediaType()))
        } else if (request is Map<*, *>) {
            if (method == HttpMethod.POST) {
                val formBodyBuilder = FormBody.Builder()
                for ((key, value) in request) {
                    formBodyBuilder.add(key.toString(), value.toString())
                }
                requestBuilder.post(formBodyBuilder.build())
            } else {
                requestBuilder.get()
            }
        } else if (request is RequestBody) {
            if (method == HttpMethod.POST) {
                requestBuilder.post(request)
            } else {
                requestBuilder.get()
            }
        }
        val requestCall = httpClient.newCall(requestBuilder.build())
        return requestCall.execute()
    }


    override fun <T> sendRequest(method: HttpMethod, url: String, request: Any, callback: OkCallback<T>) {
        sendRequest(method, url, request, mutableMapOf(), callback)
    }

    override fun <T> sendRequest(
        method: HttpMethod,
        url: String,
        request: Any,
        headers: Map<String, String>?,
        callback: OkCallback<T>
    ) {
        val requestBuilder = Request.Builder()
        requestBuilder.url(url)
        if (!headers.isNullOrEmpty()) {
            val headersBuilder = Headers.Builder()
            for ((key, value) in headers) {
                headersBuilder.add(key, value)
            }
            requestBuilder.headers(headersBuilder.build())
        }
        if (request is String) {
            if (method == HttpMethod.POST) {
                requestBuilder.post(request.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            } else {
                requestBuilder.get()
            }
        } else if (request is File) {
            requestBuilder.post(request.asRequestBody("application/octet-stream".toMediaType()))
        } else if (request is Map<*, *>) {
            if (method == HttpMethod.POST) {
                val formBodyBuilder = FormBody.Builder()
                for ((key, value) in request) {
                    formBodyBuilder.add(key.toString(), value.toString())
                }
                requestBuilder.post(formBodyBuilder.build())
            } else {
                requestBuilder.get()
            }
        } else if (request is RequestBody) {
            if (method == HttpMethod.POST) {
                requestBuilder.post(request)
            } else {
                requestBuilder.get()
            }
        }
        val requestCall = httpClient.newCall(requestBuilder.build())
        requestCall.enqueue(OkHttpCallback(callback))
    }

    private fun <T> request(
        builder: Request.Builder,
        headers: Map<String, String>?, callback: OkCallback<T>?,
    ) {
        addHeaders(headers, builder)
        httpClient.newCall(builder.build()).enqueue(OkHttpCallback(callback))
    }

    private fun <T> syncRequest(
        builder: Request.Builder,
        headers: Map<String, String>?, callback: OkCallback<T>?,
    ): Response {
        addHeaders(headers, builder)
        try {
            val call = httpClient.newCall(builder.build())
            val response = call.execute()
            if (callback != null) {
                val httpCallback = OkHttpCallback(callback)
                httpCallback.onResponse(call, response)
            }
            return response
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Response.Builder()
            .code(400)
            .build()
    }

    private fun addHeaders(headers: Map<String, String>?, builder: Request.Builder) {
        if (headers != null && headers.isNotEmpty()) {
            builder.headers(headers.toHeaders())
        }
    }

    protected fun buildRequestBody(map: Map<String, Any?>): RequestBody {
        return buildRequestBody("application/json; charset=utf-8".toMediaType(), map)
    }

    protected fun buildRequestBody(mediaType: MediaType?, map: Map<String, Any?>): RequestBody {
        //补全请求地址
        val builder = MultipartBody.Builder()
        //设置类型
        builder.setType(MultipartBody.FORM)
        val keys = map.keys
        //追加参数
        for (key in keys) {
            val value = map[key]
            if (value is File) {
                builder.addFormDataPart(key, value.name, value.asRequestBody(mediaType))
            } else if (value != null) {
                builder.addFormDataPart(key, value.toString())
            }
        }
        return builder.build()
    }

//    override fun <T> get(url: String, callback: OkCallback<T>?) {
//        get(url, null, null, callback)
//    }
//
//    override fun <T> syncGet(url: String): Response {
//        return syncGet<Any>(url, null)
//    }
//
//    override fun <T> syncGet(url: String, callback: OkCallback<T>?): Response {
//        return syncGet(url, null, null, callback)
//    }
//
//    override fun <T> get(url: String, headers: HashMap<String, String>?, callback: OkCallback<T>?) {
//        get(url, null, headers, callback)
//    }
//
//    override fun <T> get(url: String, params: OkRequestParams?, callback: OkCallback<T>?) {
//        val builder = Request.Builder()
//        val httpUrl = url.toHttpUrlOrNull()
//        if (httpUrl != null && params != null) {
//            builder.url(params.buildQueryParameter(httpUrl))
//        }
//        request(builder.get(), params?.headers, callback)
//    }
//
//    override fun <T> syncGet(url: String, params: OkRequestParams?, callback: OkCallback<T>?): Response {
//        val builder = Request.Builder()
//        val httpUrl = url.toHttpUrlOrNull()
//        if (httpUrl != null && params != null) {
//            builder.url(params.buildQueryParameter(httpUrl))
//        }
//        return syncRequest(builder.get(), params?.headers, callback)
//    }
//
//    override fun <T> get(
//            url: String, params: Map<String, Any?>?, headers: HashMap<String, String>?,
//            callback: OkCallback<T>?,
//    ) {
//        val builder = Request.Builder()
//        val httpUrl = url.toHttpUrlOrNull()
//        if (httpUrl != null) {
//            val httpBuilder: HttpUrl.Builder = httpUrl.newBuilder()
//            Util.buildGetParams(httpBuilder, params)
//            builder.url(httpBuilder.build())
//        }
//        request(builder.get(), headers, callback)
//    }
//
//    override fun <T> syncGet(url: String, params: Map<String, Any?>?, headers: HashMap<String, String>?, callback: OkCallback<T>?): Response {
//        val builder = Request.Builder()
//        val httpUrl = url.toHttpUrlOrNull()
//        if (httpUrl != null) {
//            val httpBuilder = httpUrl.newBuilder()
//            Util.buildGetParams(httpBuilder, params)
//            builder.url(httpBuilder.build())
//        }
//        return syncRequest(builder.get(), headers, callback)
//    }
//
//    override fun <T> post(url: String, params: String, callback: OkCallback<T>?) {
//        val builder = Request.Builder()
//        builder.url(url)
//        builder.post(params.toRequestBody("application/json; charset=utf-8".toMediaType()))
//        request(builder, null, callback)
//    }
//
//    override fun <T> post(url: String, params: OkRequestParams, callback: OkCallback<T>?) {
//        val builder = Request.Builder()
//        builder.url(url)
//        builder.post(params.createRequestBody())
//        request(builder, params.headers, callback)
//    }
//
//    override fun <T> post(url: String, params: Map<String, Any>, callback: OkCallback<T>?) {
//        val builder = Request.Builder()
//        builder.url(url)
//        builder.post(buildRequestBody(params))
//        request(builder, null, callback)
//    }
//
//    override fun <T> syncPost(url: String, params: OkRequestParams): Response {
//        return syncPost<Any>(url, params, null)
//    }
//
//    override fun <T> syncPost(url: String, params: OkRequestParams, callback: OkCallback<T>?): Response {
//        val builder = Request.Builder()
//        builder.url(url)
//        builder.post(params.createRequestBody())
//        return syncRequest(builder, params.headers, callback)
//    }
//
//    override fun <T> postFile(url: String, params: OkRequestParams, callback: OkCallback<T>?) {
//        val builder = Request.Builder()
//        builder.url(url)
//        builder.post(params.createFileRequestBody())
//        request(builder, params.headers, callback)
//    }
//
//    override fun <T> syncPostFile(url: String, params: OkRequestParams, callback: OkCallback<T>?): Response {
//        val builder = Request.Builder()
//        builder.url(url)
//        builder.post(params.createFileRequestBody())
//        return syncRequest(builder, params.headers, callback)
//    }
//
//    override fun <T> post(
//            url: String, params: Map<String, Any?>, headers: HashMap<String, String>?,
//            callback: OkCallback<T>?,
//    ) {
//        val builder = Request.Builder()
//        builder.url(url)
//        builder.post(buildRequestBody(params))
//        request(builder, headers, callback)
//    }
//
//    override fun <T> post(
//            url: String, json: String, headers: HashMap<String, String>?,
//            contentType: String?, callback: OkCallback<T>?,
//    ) {
//        val builder = Request.Builder()
//        builder.url(url)
//        builder.post(json.toRequestBody(contentType?.toMediaType()))
//        request(builder, headers, callback)
//    }
//
//    override fun <T> syncPost(url: String, json: String, headers: HashMap<String, String>?, contentType: String?, callback: OkCallback<T>?): Response {
//        val builder = Request.Builder()
//        builder.url(url)
//        builder.post(json.toRequestBody(contentType?.toMediaType()))
//        return syncRequest(builder, headers, callback)
//    }
//
//    override fun <T> delete(url: String, params: OkRequestParams?, callback: OkCallback<T>?) {
//        val builder = Request.Builder()
//        builder.url(url)
//        if (params != null) {
//            builder.delete(params.createRequestBody())
//        }
//        request(builder, params?.headers, callback)
//    }
//
//    override fun <T> syncDelete(url: String, params: OkRequestParams?, callback: OkCallback<T>?): Response {
//        val builder = Request.Builder()
//        builder.url(url)
//        if (params != null) builder.delete(params.createRequestBody())
//        return syncRequest(builder, params?.headers, callback)
//    }
//
//    override fun <T> delete(
//            url: String, json: String?, headers: HashMap<String, String>?,
//            contentType: String?, callback: OkCallback<T>?,
//    ) {
//        val builder = Request.Builder()
//        builder.url(url)
//        if (!json.isNullOrEmpty()) {
//            builder.delete(json.toRequestBody(contentType?.toMediaType()))
//        }
//        request(builder, headers, callback)
//    }
//
//    override fun <T> delete(
//            url: String, params: Map<String, Any?>?, headers: HashMap<String, String>?,
//            callback: OkCallback<T>?,
//    ) {
//        val builder = Request.Builder()
//        builder.url(url)
//        builder.delete(OkRequestParams(params).createRequestBody())
//        request(builder, headers, callback)
//    }
//
//    override fun <T> syncDelete(
//            url: String, params: Map<String, Any?>?,
//            headers: HashMap<String, String>?, callback: OkCallback<T>?,
//    ): Response {
//        val builder = Request.Builder()
//        builder.url(url)
//        builder.delete(OkRequestParams(params).createRequestBody())
//        return syncRequest(builder, headers, callback)
//    }
//
//    override fun <T> put(url: String, params: OkRequestParams, callback: OkCallback<T>?) {
//        val builder = Request.Builder()
//        builder.url(url)
//        builder.put(params.createRequestBody())
//        request(builder, params.headers, callback)
//    }
//
//    override fun <T> syncPut(url: String, params: OkRequestParams, callback: OkCallback<T>?): Response {
//        val builder = Request.Builder()
//        builder.url(url)
//        builder.put(params.createRequestBody())
//        return syncRequest(builder, params.headers, callback)
//    }
//
//    override fun <T> put(
//            url: String, json: String, headers: HashMap<String, String>?,
//            contentType: String?, callback: OkCallback<T>?,
//    ) {
//        val builder = Request.Builder()
//        builder.url(url)
//        builder.put(json.toRequestBody(contentType?.toMediaType()))
//        request(builder, headers, callback)
//    }
//
//    override fun <T> put(
//            url: String, params: Map<String, Any?>, headers: HashMap<String, String>?,
//            callback: OkCallback<T>?,
//    ) {
//        val builder = Request.Builder()
//        builder.url(url)
//        builder.put(buildRequestBody(params))
//        request(builder, headers, callback)
//    }
//
//    override fun <T> syncPut(url: String, params: Map<String, Any?>, headers: HashMap<String, String>?, callback: OkCallback<T>?): Response {
//        val builder = Request.Builder()
//        builder.url(url)
//        builder.put(buildRequestBody(params))
//        return syncRequest(builder, headers, callback)
//    }
    /**
     * 取消请求
     *
     * @param tag
     * @author dingpeihua
     * @date 2020/6/30 20:18
     * @version 1.0
     */
    fun cancel(tag: Any) {
        val dispatcher = httpClient.dispatcher
        for (call in dispatcher.queuedCalls()) {
            if (tag == call.request().tag()) {
                call.cancel()
            }
        }
        for (call in dispatcher.runningCalls()) {
            if (tag == call.request().tag()) {
                call.cancel()
            }
        }
    }

    companion object {
        fun getX509TrustManager(): X509TrustManager {
            return object : X509TrustManager {

                override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {

                }

                override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {

                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }
        }

        fun getSslSocketFactory(trustManager: X509TrustManager): SSLSocketFactory {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
            return sslContext.socketFactory
        }
    }
}