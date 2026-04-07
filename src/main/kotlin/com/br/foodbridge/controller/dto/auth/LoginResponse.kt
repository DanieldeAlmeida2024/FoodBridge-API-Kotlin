package com.br.foodbridge.controller.dto.auth

import com.br.foodbridge.controller.dto.organizacao.OrganizacaoResumoDTO
import com.br.foodbridge.domain.enums.UserStatus

data class LoginResponse(
    val tempToken: String,
    val nome: String?,
    val email: String?,
    val organizacoes: List<OrganizacaoResumoDTO>,
    val status: UserStatus?
)