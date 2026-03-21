package com.br.foodbridge.middleware

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AdminInterceptor : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {

        val role = RequestContext.getRole()
        println(role)
        if (request.requestURI.startsWith("/admin")) {
            println(role)
            if (role != "ADMIN") {
                response.status = 403
                response.writer.write("Acesso negado")
                return false
            }
        }

        return true
    }
}