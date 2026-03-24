package com.br.foodbridge.controller

import com.br.foodbridge.controller.dto.auth.AuthResponse
import com.br.foodbridge.controller.dto.auth.LoginRequest
import com.br.foodbridge.controller.dto.auth.SelectOrganizationRequest
import com.br.foodbridge.service.AuthService
import com.br.foodbridge.service.utils.JwtService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtService: JwtService
) {

    // LOGIN
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest) =
        authService.login(request)

    // SELECT ORG COM TOKEN
    @PostMapping("/select-org")
    fun selectOrg(
        @RequestHeader("Authorization") authHeader: String,
        @RequestBody request: SelectOrganizationRequest
    ): AuthResponse {
        val token = authHeader.removePrefix("Bearer ")

        val tokenData = jwtService.generateAccessFromTemp(
            token,
            request.organizacaoId
        )

        return AuthResponse(
            token = tokenData.token,
            usuarioId = tokenData.usuarioId,
            vinculoId = tokenData.vinculoId,
            organizacaoId = tokenData.organizacaoId,
            role = tokenData.role
        )
    }
}

