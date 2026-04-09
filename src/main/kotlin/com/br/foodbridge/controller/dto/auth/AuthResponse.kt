package com.br.foodbridge.controller.dto.auth

data class AuthResponse(
    val token: String?,
    val usuarioId: Long?,
    val vinculoId: Long?,
    val organizacaoId: Long?,
    val role: String?
)
