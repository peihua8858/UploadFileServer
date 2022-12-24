@file:JvmName("LogUtil")
@file:JvmMultifileClass

package com.example.uploadfileserver

import com.example.uploadfileserver.websocket.LogWebSocket
import com.example.uploadfileserver.websocket.MessageResponse
import org.slf4j.LoggerFactory

fun <T : Any> T.wLog(lazyMessage: () -> String) {
    val message = lazyMessage()
    LoggerFactory.getLogger(this.javaClass).warn(message)
    sendLog(message)
}

fun <T : Any> T.wLog(throwable: Throwable?, lazyMessage: () -> String) {
    val message = lazyMessage()
    LoggerFactory.getLogger(this.javaClass).warn(message, throwable)
    sendLog(message, throwable)
}

fun <T : Any> T.eLog(lazyMessage: () -> String) {
    val message = lazyMessage()
    LoggerFactory.getLogger(this.javaClass).error(message)
    sendLog(message)
}

fun <T : Any> T.eLog(throwable: Throwable?, lazyMessage: () -> String) {
    val message = lazyMessage()
    LoggerFactory.getLogger(this.javaClass).error(message, throwable)
    sendLog(message, throwable)
}

fun <T : Any> T.dLog(lazyMessage: () -> String) {
    val message = lazyMessage()
    LoggerFactory.getLogger(this.javaClass).debug(message)
    sendLog(message)
}

fun <T : Any> T.dLog(throwable: Throwable, lazyMessage: () -> String) {
    val message = lazyMessage()
    LoggerFactory.getLogger(this.javaClass).debug(message, throwable)
    sendLog(message, throwable)
}

fun <T : Any> T.iLog(lazyMessage: () -> String) {
    val message = lazyMessage()
    LoggerFactory.getLogger(this.javaClass).info(message)
    sendLog(message)
}

fun <T : Any> T.iLog(throwable: Throwable, lazyMessage: () -> String) {
    val message = lazyMessage()
    LoggerFactory.getLogger(this.javaClass).info(message, throwable)
    sendLog(message, throwable)
}

private fun sendLog(message: String, throwable: Throwable? = null) {
    val msg = message + if (throwable != null) "\n" + throwable.stackTraceToString() else ""
    LogWebSocket.sendMessage(MessageResponse.msg<String>(1, msg))
}