package com.br.foodbridge.config

import com.br.foodbridge.middleware.AdminInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val adminInterceptor: AdminInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(adminInterceptor)
    }
}