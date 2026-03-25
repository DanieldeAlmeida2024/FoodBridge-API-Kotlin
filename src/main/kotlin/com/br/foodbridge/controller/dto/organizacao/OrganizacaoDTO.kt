package com.br.foodbridge.controller.dto.organizacao

import com.br.foodbridge.domain.enums.StatusOrganizacao
import com.br.foodbridge.domain.model.Endereco
import com.br.foodbridge.domain.model.UsuarioOrganizacao
import com.br.foodbridge.domain.model.VoluntarioOrganizacao
import jakarta.validation.Valid
import org.jetbrains.annotations.NotNull

data class OrganizacaoDTO(

    val cnpj: String,
    val email: String,
    val telefone: String,
    val status: StatusOrganizacao,
    val id: Long?,
    val nome: String,
    val usuarios: MutableList<UsuarioOrganizacao>,
    val voluntarios: MutableList<VoluntarioOrganizacao>,
    @field:NotNull("Endereço é obrigatório")
    @field:Valid
    val endereco: Endereco,
)
