package com.example.uploadfileserver

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class MainController {
    @RequestMapping("index")
    fun home(): String {
        return "/templates/index.html"
    }
}