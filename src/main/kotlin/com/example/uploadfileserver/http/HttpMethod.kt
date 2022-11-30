package com.example.uploadfileserver.http

import com.google.gson.annotations.SerializedName

enum class HttpMethod {
    @SerializedName("POST")
    POST,
    @SerializedName("GET")
    GET,
    @SerializedName("PUT")
    PUT,
    @SerializedName("DELETE")
    DELETE
}