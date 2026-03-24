package com.br.foodbridge.controller.dto.voluntario

import com.br.foodbridge.domain.enums.StatusVoluntario
import com.br.foodbridge.domain.model.Organizacao
import com.br.foodbridge.domain.model.Voluntario
import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class VoluntarioOrganizacao(
    val id: Long,
    val voluntario: Voluntario,
    val organizacao: Organizacao,
    var status: StatusVoluntario = StatusVoluntario.ATIVO
)
