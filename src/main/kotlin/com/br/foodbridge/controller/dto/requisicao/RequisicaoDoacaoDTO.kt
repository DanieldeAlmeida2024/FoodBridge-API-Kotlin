package com.br.foodbridge.controller.dto.requisicao

import com.br.foodbridge.domain.enums.StatusDoacao
import com.br.foodbridge.domain.enums.StatusReivindicacao
import java.time.LocalDateTime

data class RequisicaoDoacaoDTO(
    val id: Long,
    val doacaoId: Long,
    val organizacaoSolicitanteId: Long,
    val organizacaoSolicitanteNome: String,
    val organizacaoDoadoraId: Long,
    val organizacaoDoadoraNome: String,
    val voluntarioId: Long?,
    val voluntarioNome: String?,
    val quantidadeSolicitada: Double,
    val observacao: String?,
    val status: StatusReivindicacao,
    val statusDoacao: StatusDoacao?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val respondedAt: LocalDateTime?,
    val completedAt: LocalDateTime?
)
