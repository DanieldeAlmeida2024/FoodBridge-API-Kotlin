package com.br.foodbridge.controller.dto.requisicao

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull

data class CreateRequisicaoDoacaoRequest(
    @field:NotNull(message = "ID da doação é obrigatório")
    val doacaoId: Long?,

    @field:DecimalMin(value = "0.1", message = "Quantidade solicitada deve ser maior que zero")
    val quantidadeSolicitada: Double,

    val observacao: String? = null
)
