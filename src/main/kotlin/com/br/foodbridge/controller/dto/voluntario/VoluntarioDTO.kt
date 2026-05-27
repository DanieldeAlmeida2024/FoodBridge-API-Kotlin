package com.br.foodbridge.controller.dto.voluntario

import com.br.foodbridge.domain.enums.StatusVoluntario
import com.br.foodbridge.domain.model.Endereco
import com.br.foodbridge.domain.model.Organizacao
import com.br.foodbridge.domain.model.Voluntario
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.br.CPF
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime

data class VoluntarioDTO(
    val id: Long?,
    val nome: String,
    val telefone: String,
    val cpf: String,
    val email: String,
    @field:NotNull("Endereço é obrigatório")
    @field:Valid
    val endereco: Endereco,
)
data class CreateUpdateVoluntario(
    val id: Long? = null,

    @field:NotBlank(message ="Nome é obrigatório")
    val nome: String,

    @field:Pattern(regexp = "\\d{10,13}", message = "Telefone deve conter entre 10 e 13 digitos")
    val telefone: String,

    @field:Email(message ="É obrigatório o cadastro de um e-mail")
    val email: String,

    @field:NotNull("Endereço é obrigatório")
    @field:Valid
    val endereco: Endereco,

    @field:CPF(message ="CPF é obrigatório")
    val cpf: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
data class VoluntarioOrganizacao(
    val id: Long,
    val voluntario: Voluntario,
    val organizacao: Organizacao,
    var status: StatusVoluntario = StatusVoluntario.ATIVO
)
