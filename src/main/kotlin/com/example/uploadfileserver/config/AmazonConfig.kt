package com.example.uploadfileserver.config

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AmazonConfig {
    @Bean
    fun s3(@Qualifier("Credentials") credentials: Credentials): AmazonS3 {
        val awsCredentials: AWSCredentials = BasicAWSCredentials(credentials.accessKey, credentials.secretKey)
        return AmazonS3ClientBuilder
            .standard()
            .withRegion(Regions.US_EAST_1)
            .withCredentials(AWSStaticCredentialsProvider(awsCredentials))
            .build()
    }
}