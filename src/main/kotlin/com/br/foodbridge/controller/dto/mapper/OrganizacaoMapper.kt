package com.br.foodbridge.controller.dto.mapper

import com.br.foodbridge.controller.dto.organizacao.OrganizacaoDTO
import com.br.foodbridge.domain.model.Organizacao

object OrganizacaoMapper {

    fun toResponse(org: Organizacao) = OrganizacaoDTO(
        id = org.id,
        nome = org.nome,
        cnpj = org.cnpj,
        email = org.email,
        telefone = org.telefone,
        status = org.status,
        usuarios = org.usuarios
    )
}