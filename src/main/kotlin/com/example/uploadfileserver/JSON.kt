package com.example.uploadfileserver

import com.peihua8858.GsonFactory
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

object JSON {
    fun toJSONString(obj: Any?): String {
        return GsonFactory.defaultBuilder().create().toJson(obj)
    }

    fun parseObject(source: String?): Map<String, Any> {
        return parseObject(source, object : TypeToken<Map<String?, Any?>?>() {}.type)
    }

    fun <T> parseObject(source: String?, rawType: Type?): T {
        return GsonFactory.defaultBuilder().create().fromJson(source, rawType)
    }
}