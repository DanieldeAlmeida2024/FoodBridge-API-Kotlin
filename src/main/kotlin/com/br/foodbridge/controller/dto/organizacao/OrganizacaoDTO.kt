package com.br.foodbridge.controller.dto.organizacao

import com.br.foodbridge.domain.enums.StatusOrganizacao
import com.br.foodbridge.domain.model.UsuarioOrganizacao

data class OrganizacaoDTO(

    val cnpj: String,
    val email: String,
    val telefone: String,
    val status: StatusOrganizacao,
    val id: Long?,
    val nome: String,
    val usuarios: MutableList<UsuarioOrganizacao>
)
