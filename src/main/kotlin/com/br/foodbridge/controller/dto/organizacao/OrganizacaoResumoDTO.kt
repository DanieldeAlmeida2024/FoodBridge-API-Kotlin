package com.br.foodbridge.controller.dto.organizacao

import com.br.foodbridge.domain.enums.OrganizacaoRole
import com.br.foodbridge.domain.enums.StatusOrganizacao

data class OrganizacaoResumoDTO(
    val organizacaoId: Long,
    val nome: String?,
    val role: OrganizacaoRole,
    val status: StatusOrganizacao
)
