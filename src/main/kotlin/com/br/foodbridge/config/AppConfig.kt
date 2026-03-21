package com.br.foodbridge.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app.admin")
class AdminConfig {
    lateinit var email: String
    lateinit var password: String
}

