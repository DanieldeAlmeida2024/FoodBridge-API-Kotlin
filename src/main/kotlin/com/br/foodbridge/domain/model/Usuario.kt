package com.br.foodbridge.domain.model

import com.br.foodbridge.domain.enums.UserRole
import com.br.foodbridge.domain.enums.UserStatus

data class Usuario(
    val id: Int,
    val email: String,
    val senha: String,
    val nome: String,
    val role: UserRole,
    val status: UserStatus
)
