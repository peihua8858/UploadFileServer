package com.example.uploadfileserver.http

/**
 * 请求返回
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/3/13 17:54
 */
interface OkCallback<T> {
    fun onSuccess(response: T?)
    fun onFailure(e: Throwable?)
}