package com.br.foodbridge.controller.dto.organizacao

import com.br.foodbridge.domain.enums.OrganizacaoRole
import com.br.foodbridge.domain.enums.StatusOrganizacao
import java.time.LocalDateTime

data class CreateUpdateOrganizacaoRequest(
    val nome: String,
    val cnpj: String,
    val description: String?,
    val telefone: String ,
    val email: String,
    val website: String?,
    val role: OrganizacaoRole,
    val status: StatusOrganizacao = StatusOrganizacao.REVISAO,
    val verificationDate: LocalDateTime?,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
