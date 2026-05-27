package com.br.foodbridge.controller.dto.organizacao

import com.br.foodbridge.annotation.ValidOrganizacaoRole
import com.br.foodbridge.domain.enums.OrganizacaoRole
import com.br.foodbridge.domain.enums.StatusOrganizacao
import com.br.foodbridge.domain.model.Endereco
import com.br.foodbridge.domain.model.UsuarioOrganizacao
import com.br.foodbridge.domain.model.VoluntarioOrganizacao
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime

data class OrganizacaoDTO(

    val cnpj: String,
    val email: String,
    val telefone: String,
    val status: StatusOrganizacao,
    val id: Long?,
    val nome: String,
    val usuarios: MutableList<UsuarioOrganizacao>,
    val role: OrganizacaoRole?,
    val voluntarios: MutableList<VoluntarioOrganizacao>,
    @field:NotNull("Endereço é obrigatório")
    @field:Valid
    val endereco: Endereco,
)

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

    @field:ValidOrganizacaoRole
    val role: OrganizacaoRole,

    val status: StatusOrganizacao = StatusOrganizacao.REVISAO,
    @field:NotNull("Endereço é obrigatório")
    @field:Valid
    val endereco: Endereco,
    val verificationDate: LocalDateTime?,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

data class OrganizacaoResumoDTO(
    val organizacaoId: Long,
    val nome: String?,
    val role: OrganizacaoRole,
    val status: StatusOrganizacao
)
