package com.example.uploadfileserver.http

class CodeException(val code: Int, message: String) : Exception(message){
}