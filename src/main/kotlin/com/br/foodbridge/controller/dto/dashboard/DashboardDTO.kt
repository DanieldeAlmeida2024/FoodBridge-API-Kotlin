package com.br.foodbridge.controller.dto.dashboard

data class DashboardPublicoDTO(
    val doacoesDisponiveis: Int,
    val quantidadeDisponivel: Double,
    val organizacoesDoadoras: Int,
    val ongsAtivas: Int,
    val doacoesConcluidas: Int
)

data class DashboardDoadorDTO(
    val totalDoacoes: Int,
    val doacoesPublicadas: Int,
    val doacoesParciais: Int,
    val doacoesCompletas: Int,
    val requisicoesPendentes: Int,
    val coletasAguardando: Int,
    val coletasConcluidas: Int,
    val quantidadeDoada: Double
)

data class DashboardOngDTO(
    val requisicoesPendentes: Int,
    val requisicoesAprovadas: Int,
    val requisicoesConcluidas: Int,
    val requisicoesCanceladas: Int,
    val doacoesDisponiveis: Int,
    val voluntariosAtivos: Int,
    val quantidadeRecebida: Double
)
