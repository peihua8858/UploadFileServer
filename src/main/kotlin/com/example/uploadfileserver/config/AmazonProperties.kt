package com.example.uploadfileserver.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class AmazonProperties {
    @Bean(name = ["Credentials"])
    @Qualifier("Credentials")
    @ConfigurationProperties(prefix = "credentials", ignoreUnknownFields = true)
    fun credentials(): Credentials {
        return Credentials()
    }

}

class Credentials {
    var accessKey: String? = null
    var secretKey: String? = null
}