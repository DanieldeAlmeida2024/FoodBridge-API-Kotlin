package com.br.foodbridge.controller.dto.voluntario

import com.br.foodbridge.domain.model.Endereco
import jakarta.validation.Valid
import org.jetbrains.annotations.NotNull

data class VoluntarioDTO(
    val id: Long?,
    val nome: String,
    val telefone: String,
    val email: String,
    @field:NotNull("Endereço é obrigatório")
    @field:Valid
    val endereco: Endereco,
)
