package com.br.foodbridge.controller.dto.usuario

import com.br.foodbridge.domain.enums.UserStatus
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateUpdateUserRequest(

    val usuarioid: Long? = null,
    @field:NotBlank(message = "Nome é obrigatório")
    val nome: String = "",

    @field:Email(message = "Email inválido")
    val email: String = "",

    @field:NotBlank(message = "Senha é obrigatória")
    val senha: String = "",

    val status: UserStatus = UserStatus.PENDENTE_VERIFICACAO
)