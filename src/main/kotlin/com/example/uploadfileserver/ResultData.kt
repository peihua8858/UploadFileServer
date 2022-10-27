package com.example.uploadfileserver

data class ResultData(
    val name: String?,
    val size: Long,
    val thumbnailUrl: String,
    val type: String?,
    val url: String
)