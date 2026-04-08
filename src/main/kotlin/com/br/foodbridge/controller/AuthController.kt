package com.br.foodbridge.controller

import com.br.foodbridge.controller.dto.auth.AuthResponse
import com.br.foodbridge.controller.dto.auth.LoginRequest
import com.br.foodbridge.controller.dto.auth.LoginResponse
import com.br.foodbridge.controller.dto.auth.SelectOrganizationRequest
import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.service.AuthService
import com.br.foodbridge.service.utils.JwtService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
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

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: LoginRequest): ResponseEntity<LoginResponse> {
        val loginResponse = authService.login(request)

        val authResponse = LoginResponse(
            tempToken = loginResponse.tempToken,
            organizacoes = loginResponse.organizacoes,
            email = loginResponse.email,
            status = loginResponse.status,
            nome = loginResponse.nome,
        )

        return ResponseEntity.ok(authResponse)
    }

    @PostMapping("/select-org")
    fun selectOrg(
        @AuthenticationPrincipal tokenData: TokenData,
        @RequestBody request: SelectOrganizationRequest
    ): ResponseEntity<AuthResponse> {

        val loginResponse = try {
            authService.selectOrganization(
                usuarioId = tokenData.usuarioId,
                organizacaoId = request.organizacaoId
            )
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
        }

        val authResponse = AuthResponse(
            token = loginResponse.tempToken,
            usuarioId = tokenData.usuarioId,
            vinculoId = loginResponse.organizacoes.firstOrNull { it.organizacaoId == request.organizacaoId }?.organizacaoId,
            organizacaoId = request.organizacaoId,
            role = loginResponse.organizacoes.firstOrNull { it.organizacaoId == request.organizacaoId }?.role?.name
        )

        return ResponseEntity.ok(authResponse)
    }
}

