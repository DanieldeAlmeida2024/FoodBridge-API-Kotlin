package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.dashboard.ImpactoAlimentarDTO
import com.br.foodbridge.controller.dto.dashboard.ImpactoCategoriaDTO
import com.br.foodbridge.domain.enums.StatusDoacao
import com.br.foodbridge.domain.enums.StatusReivindicacao
import com.br.foodbridge.domain.enums.Unidade
import com.br.foodbridge.domain.model.Doacao
import com.br.foodbridge.domain.model.RequisicaoDoacao
import org.springframework.stereotype.Component

@Component
class ImpactoAlimentarCalculator {
    /** Each completed collection contributes once, in the donation's own unit. */
    fun calcular(
        requisicoes: List<RequisicaoDoacao>,
        doacoesFechadas: List<Doacao> = emptyList()
    ): ImpactoAlimentarDTO {
        val concluidas = requisicoes.filter { it.status == StatusReivindicacao.CONCLUIDO }
        val coletadoPorDoacao = concluidas.groupBy { it.doacao.id }
            .mapValues { (_, coletas) -> coletas.sumOf { it.quantidadeSolicitada } }

        // A manual closure records the final total, not an additional collection.
        // Include only the part not already represented by completed requests.
        // ONGs pass no manual closures: unassigned amounts cannot be attributed to them.
        val complementos = doacoesFechadas.distinctBy { it.id }
            .filter { it.status == StatusDoacao.COMPLETO }
            .mapNotNull { doacao ->
                val quantidadeFinal = doacao.quantidadeColetadaFinal ?: return@mapNotNull null
                val complemento = (quantidadeFinal - (coletadoPorDoacao[doacao.id] ?: 0.0)).coerceAtLeast(0.0)
                if (complemento > 0.0) doacao to complemento else null
            }
        val parcelas = concluidas.map { it.doacao to it.quantidadeSolicitada } + complementos
        val porCategoria = parcelas.groupBy { (d, _) -> d.tipoComida to d.unidade }
            .map { (chave, valores) -> ImpactoCategoriaDTO(chave.first, chave.second, valores.sumOf { it.second }) }
            .sortedWith(compareBy({ it.tipoComida.ordinal }, { it.unidade.ordinal }))

        return ImpactoAlimentarDTO(
            quantidadesPorUnidade = Unidade.entries.associateWith { unidade ->
                parcelas.filter { it.first.unidade == unidade }.sumOf { it.second }
            },
            coletasConcluidas = concluidas.size,
            doacoesAproveitadas = parcelas.mapNotNull { it.first.id }.distinct().size,
            organizacoesBeneficiadas = concluidas.mapNotNull { it.organizacaoSolicitante.id }.distinct().size,
            voluntariosParticipantes = concluidas.mapNotNull { it.voluntario?.id }.distinct().size,
            categoriasDeAlimentos = parcelas.map { it.first.tipoComida }.distinct().size,
            porCategoria = porCategoria,
            fechamentosManuais = complementos.size,
            incluiDadosDemonstrativos = parcelas.any { it.first.descricaoComida.contains("[DEMO ESCOLAR", ignoreCase = true) } ||
                concluidas.any { it.observacao?.contains("[DEMO ESCOLAR", ignoreCase = true) == true }
        )
    }
}
