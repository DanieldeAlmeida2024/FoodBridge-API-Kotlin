package com.br.foodbridge.controller.dto.auth

import com.br.foodbridge.controller.dto.organizacao.OrganizacaoResumoDTO

data class LoginResponse(
    val tempToken: String,
    val nome: String?,
    val email: String?,
    val organizacoes: List<OrganizacaoResumoDTO>
)