package com.br.foodbridge.controller

import com.br.foodbridge.controller.dto.auth.AuthResponse
import com.br.foodbridge.controller.dto.auth.LoginRequest
import com.br.foodbridge.controller.dto.auth.LoginResponse
import com.br.foodbridge.controller.dto.auth.SelectOrganizationRequest
import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.domain.enums.UserStatus
import com.br.foodbridge.exception.custom.BusinessException
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
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(
        @RequestBody @Valid request: LoginRequest
    ): ResponseEntity<LoginResponse> {

        val response = authService.login(request)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/select-org")
    fun selectOrg(
        @AuthenticationPrincipal tokenData: TokenData,
        @RequestBody @Valid request: SelectOrganizationRequest
    ): ResponseEntity<out Any?> {

        println(tokenData.status)
        if (tokenData.status != "VERIFICADO") {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(AuthResponseError("Usuário ainda não foi autorizado"))
        }

        val loginResponse = authService.selectOrganization(
            usuarioId = tokenData.usuarioId,
            organizacaoId = request.organizacaoId
        )

        val organizacaoSelecionada = loginResponse.organizacoes
            .firstOrNull { it.organizacaoId == request.organizacaoId }
            ?: throw BusinessException("Organização não encontrada no contexto do usuário")

        val response = AuthResponse(
            token = loginResponse.accessToken
                ?: throw BusinessException("Token de acesso não gerado"),
            usuarioId = tokenData.usuarioId,
            vinculoId = organizacaoSelecionada.organizacaoId,
            organizacaoId = request.organizacaoId,
            role = organizacaoSelecionada.role.name
        )

        return ResponseEntity.ok(response)
    }

    // Classe de erro personalizada para respostas 403
    data class AuthResponseError(val mensagem: String)
}

