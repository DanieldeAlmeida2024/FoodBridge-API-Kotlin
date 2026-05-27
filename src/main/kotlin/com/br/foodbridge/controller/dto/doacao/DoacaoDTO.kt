package com.br.foodbridge.controller.dto.doacao

import com.br.foodbridge.domain.enums.StatusDoacao
import com.br.foodbridge.domain.enums.TipoDoacao
import com.br.foodbridge.domain.enums.Unidade
import com.br.foodbridge.domain.model.Endereco
import com.br.foodbridge.domain.model.JanelaHorario
import com.br.foodbridge.domain.model.Organizacao
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import org.jetbrains.annotations.NotNull as JetBrainsNotNull
import java.time.LocalDateTime

data class DoacaoDTO(
    val id: Long? = null,
    val tipoComida: TipoDoacao,
    val descricaoComida: String,
    val quantidade: Double,
    val unidade: Unidade,
    val dataExpiracao: LocalDateTime,
    val janelasDisponiveis: List<JanelaHorario> = emptyList(),
    val status: StatusDoacao?,
    @field:JetBrainsNotNull("Endereço é obrigatório")
    @field:Valid
    val endereco: Endereco,
    val organizacao: Organizacao?,
    val quantidadeColetadaFinal: Double? = null,
    val fechadoAt: LocalDateTime? = null,
    val observacaoFechamento: String? = null
)

data class FecharDoacaoRequest(
    @field:NotNull(message = "Quantidade coletada é obrigatória")
    @field:DecimalMin(value = "0.0", inclusive = true, message = "Quantidade coletada não pode ser negativa")
    val quantidadeColetada: Double,
    val observacao: String? = null
)
