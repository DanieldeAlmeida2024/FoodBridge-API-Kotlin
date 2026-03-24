package com.br.foodbridge.controller.dto.organizacao

import com.br.foodbridge.domain.enums.OrganizacaoRole
import com.br.foodbridge.domain.enums.StatusOrganizacao
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class CreateUpdateOrganizacaoRequest(
    val nome: String,
    @field:NotBlank("CNPJ é obrigatório")
    val cnpj: String,
    val description: String?,
    @field:NotBlank("Telefone é Obrigatório")
    val telefone: String,
    @field:Email("Email é obrigatório")
    val email: String,
    val website: String?,
    @field:NotBlank("É obrigatório selecionar um tipo de organização")
    val role: OrganizacaoRole,
    val status: StatusOrganizacao = StatusOrganizacao.REVISAO,
    val verificationDate: LocalDateTime?,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
