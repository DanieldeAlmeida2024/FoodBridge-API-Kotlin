package com.br.foodbridge.controller.dto.voluntario

import com.br.foodbridge.domain.enums.StatusVoluntario
import com.br.foodbridge.domain.model.VoluntarioOrganizacao
import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.br.CPF
import java.time.LocalDateTime

data class CreateUpdateVoluntario(
    val id: Long? = null,

    @field:NotBlank(message ="Nome é obrigatório")
    val nome: String,

    @field:Pattern(regexp = "\\d{12}",message ="Telefone é obrigatório")
    val telefone: String,

    @field:Email(message ="É obrigatório o cadastro de um e-mail")
    val email: String,

    @field:NotBlank(message ="Endereço é obrigatório")
    val endereco: String,

    @field:CPF(message ="CPF é obrigatório")
    val cpf: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
