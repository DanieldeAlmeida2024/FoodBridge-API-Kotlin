package com.br.foodbridge.controller.dto.dashboard

import com.br.foodbridge.domain.enums.Unidade

data class DashboardPublicoDTO(
    val doacoesDisponiveis: Int,
    val quantidadeDisponivel: Double,
    val organizacoesDoadoras: Int,
    val ongsAtivas: Int,
    val doacoesConcluidas: Int,
    val disponivelPorUnidade: Map<Unidade, Double>,
    val impacto: ImpactoAlimentarDTO
)

data class DashboardDoadorDTO(
    val totalDoacoes: Int,
    val doacoesPublicadas: Int,
    val doacoesParciais: Int,
    val doacoesCompletas: Int,
    val requisicoesPendentes: Int,
    val coletasAguardando: Int,
    val coletasConcluidas: Int,
    val quantidadeDoada: Double,
    val impacto: ImpactoAlimentarDTO
)

data class DashboardOngDTO(
    val requisicoesPendentes: Int,
    val requisicoesAprovadas: Int,
    val requisicoesConcluidas: Int,
    val requisicoesCanceladas: Int,
    val doacoesDisponiveis: Int,
    val voluntariosAtivos: Int,
    val quantidadeRecebida: Double,
    val impacto: ImpactoAlimentarDTO
)
