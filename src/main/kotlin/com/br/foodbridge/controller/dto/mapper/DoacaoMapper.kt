package com.br.foodbridge.controller.dto.mapper

import com.br.foodbridge.domain.model.Doacao

object DoacaoMapper {
    fun toResponse(doacao: Doacao)= Doacao(
        id = doacao.id,
        tipoComida = doacao.tipoComida,
        descricaoComida = doacao.descricaoComida,
        quantidade = doacao.quantidade,
        unidade = doacao.unidade,
        dataExpiracao = doacao.dataExpiracao,
        janelasDisponiveis = doacao.janelasDisponiveis,
        status = doacao.status,
        endereco = doacao.endereco,
        organizacao = doacao.organizacao
    )
}
