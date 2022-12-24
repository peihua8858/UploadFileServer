package com.example.uploadfileserver.websocket

import com.example.uploadfileserver.toJson
import javax.websocket.server.ServerEndpoint
import com.example.uploadfileserver.websocket.WebSocketConfig
import com.example.uploadfileserver.websocket.LogWebSocket
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.concurrent.CopyOnWriteArraySet
import javax.websocket.*
import kotlin.Throws
import kotlin.jvm.Synchronized

@ServerEndpoint(value = "/websocket", configurator = WebSocketConfig::class)
@Component
class LogWebSocket {
    private var session: Session? = null

    @OnOpen
    fun onOpen(session: Session?, config: EndpointConfig?) {
        this.session = session
        webSocketSet.add(session)
        addOnlineCount()
        sendSystemMessage(MessageResponse.msg("有新连接加入！当前在线人数为$onlineCount"))
    }

    fun sendSystemMessage(response: MessageResponse<Any?>) {
        response.code = SYSTEM_MSG_CODE
        sendMessage(response)
    }

    @OnClose
    fun onClose() {
        webSocketSet.remove(session)
        subOnlineCount()
        println("有一连接关闭！当前在线人数为$onlineCount")
    }

    @OnMessage
    fun onMessage(message: String, session: Session?) {
        println("来自客户端的消息:$message")
        try {
            sendMessage(session, message)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        /**
         * 系统消息
         *
         * @type {number}
         */
        private const val SYSTEM_MSG_CODE = 0

        /**
         * 用户消息
         *
         * @type {number}
         */
        private const val USER_MSG_CODE = 1

        @get:Synchronized
        var onlineCount = 0
            private set
        private val webSocketSet = CopyOnWriteArraySet<Session?>()
        fun sendUserMessage(response: MessageResponse<*>) {
            response.code = USER_MSG_CODE
            sendMessage(response)
        }

        @Throws(IOException::class)
        private fun sendMessage(session: Session?, response: MessageResponse<*>) {
            sendMessage(session, response.toJson())
        }

        @Throws(IOException::class)
        fun sendMessage(session: Session?, message: String?) {
            session!!.basicRemote.sendText(message)
        }

        fun sendMessage(response: MessageResponse<*>) {
            synchronized(webSocketSet) {
                for (item in webSocketSet) {
                    try {
                        sendMessage(item, response)
                    } catch (ignored: IOException) {
                    }
                }
            }
        }

        fun sendMessage(message: String?) {
            synchronized(webSocketSet) {
                for (item in webSocketSet) {
                    try {
                        sendMessage(item, message)
                    } catch (ignored: IOException) {
                    }
                }
            }
        }

        @Synchronized
        fun addOnlineCount() {
            onlineCount += 1
        }

        @Synchronized
        fun subOnlineCount() {
            onlineCount -= 1
        }
    }
}