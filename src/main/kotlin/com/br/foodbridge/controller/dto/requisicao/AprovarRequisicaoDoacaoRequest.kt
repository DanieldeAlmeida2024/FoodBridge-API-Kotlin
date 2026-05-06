package com.br.foodbridge.controller.dto.requisicao

import jakarta.validation.constraints.NotNull

data class AprovarRequisicaoDoacaoRequest(
    @field:NotNull(message = "ID do voluntario e obrigatorio")
    val voluntarioId: Long?
)
