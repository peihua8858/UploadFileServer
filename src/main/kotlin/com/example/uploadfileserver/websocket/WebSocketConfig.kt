package com.example.uploadfileserver.websocket

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.websocket.server.ServerEndpointConfig
import javax.websocket.server.HandshakeRequest
import javax.websocket.HandshakeResponse
import javax.servlet.http.HttpSession
import org.springframework.web.socket.server.standard.ServerEndpointExporter

@Configuration
class WebSocketConfig : ServerEndpointConfig.Configurator() {
    override fun modifyHandshake(sec: ServerEndpointConfig, request: HandshakeRequest, response: HandshakeResponse) {
        /*如果没有监听器,那么这里获取到的HttpSession是null*/
        val ssf = request.httpSession
        if (ssf != null) {
            val httpSession = request.httpSession as HttpSession
            //关键操作
            sec.userProperties["sessionId"] = httpSession.id
            println("获取到的SessionID：" + httpSession.id)
        }
    }

    @Bean
    fun serverEndpointExporter(): ServerEndpointExporter {
        //这个对象说一下，貌似只有服务器是tomcat的时候才需要配置,具体我没有研究
        return ServerEndpointExporter()
    }
}