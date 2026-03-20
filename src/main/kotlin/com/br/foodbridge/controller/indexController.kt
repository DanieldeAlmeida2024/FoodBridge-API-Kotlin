package com.br.foodbridge.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class indexController {
    @GetMapping("/")
    fun index(): String {
        return "index_page"
    }
}