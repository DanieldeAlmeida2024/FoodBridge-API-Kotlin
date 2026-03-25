package com.br.foodbridge.controller.dto.doacao

import com.br.foodbridge.domain.enums.StatusDoacao
import com.br.foodbridge.domain.enums.TipoDoacao
import com.br.foodbridge.domain.enums.Unidade
import com.br.foodbridge.domain.model.Endereco
import com.br.foodbridge.domain.model.Organizacao
import jakarta.validation.Valid
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime

data class DoacaoDTO(
    val tipoComida: TipoDoacao,
    val descricaoComida: String,
    val quantidade: Double,
    val unidade: Unidade,
    val dataExpiracao: LocalDateTime,
    val status: StatusDoacao?,
    @field:NotNull("Endereço é obrigatório")
    @field:Valid
    val endereco: Endereco,
    val organizacao: Organizacao?
)
