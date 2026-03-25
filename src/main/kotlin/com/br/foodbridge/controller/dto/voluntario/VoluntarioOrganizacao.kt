package com.br.foodbridge.controller.dto.voluntario

import com.br.foodbridge.domain.enums.StatusVoluntario
import com.br.foodbridge.domain.model.Endereco
import com.br.foodbridge.domain.model.Organizacao
import com.br.foodbridge.domain.model.Voluntario

data class VoluntarioOrganizacao(
    val id: Long,
    val voluntario: Voluntario,
    val organizacao: Organizacao,
    var status: StatusVoluntario = StatusVoluntario.ATIVO
)
