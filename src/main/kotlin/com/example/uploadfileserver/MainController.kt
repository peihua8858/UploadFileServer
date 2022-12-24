package com.example.uploadfileserver

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping(*["/"])
class MainController {
    @RequestMapping("index")
    fun home(): String {
        return "index.html"
    }

    @RequestMapping("log")
    fun logcat(): String {
        return "logcat.html"
    }
}