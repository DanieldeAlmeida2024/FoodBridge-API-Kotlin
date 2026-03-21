package com.br.foodbridge.controller.dto.usuario

import com.br.foodbridge.domain.enums.UserStatus

data class UsuarioDTO(
    val id: Long?,
    val nome: String?,
    val email: String?,
    val status: UserStatus?,
)
