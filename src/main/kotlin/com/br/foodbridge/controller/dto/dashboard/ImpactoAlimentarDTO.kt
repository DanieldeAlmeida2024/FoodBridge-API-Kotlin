package com.br.foodbridge.controller.dto.dashboard

import com.br.foodbridge.domain.enums.TipoDoacao
import com.br.foodbridge.domain.enums.Unidade

data class ImpactoCategoriaDTO(
    val tipoComida: TipoDoacao,
    val unidade: Unidade,
    val quantidade: Double
)

data class ImpactoAlimentarDTO(
    val quantidadesPorUnidade: Map<Unidade, Double>,
    val coletasConcluidas: Int,
    val doacoesAproveitadas: Int,
    val organizacoesBeneficiadas: Int,
    val voluntariosParticipantes: Int,
    val categoriasDeAlimentos: Int,
    val porCategoria: List<ImpactoCategoriaDTO>,
    val fechamentosManuais: Int,
    val incluiDadosDemonstrativos: Boolean
)
