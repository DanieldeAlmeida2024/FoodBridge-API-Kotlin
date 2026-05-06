package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.dashboard.DashboardDoadorDTO
import com.br.foodbridge.controller.dto.dashboard.DashboardOngDTO
import com.br.foodbridge.controller.dto.dashboard.DashboardPublicoDTO
import com.br.foodbridge.domain.enums.OrganizacaoRole
import com.br.foodbridge.domain.enums.StatusDoacao
import com.br.foodbridge.domain.enums.StatusReivindicacao
import com.br.foodbridge.domain.enums.StatusVoluntario
import com.br.foodbridge.domain.model.Doacao
import com.br.foodbridge.domain.repository.DoacaoRepository
import com.br.foodbridge.domain.repository.OrganizacaoRepository
import com.br.foodbridge.domain.repository.RequisicaoDoacaoRepository
import com.br.foodbridge.domain.repository.VoluntarioOrganizacaoRepository
import com.br.foodbridge.exception.custom.BusinessException
import com.br.foodbridge.exception.custom.ValidationException
import org.springframework.stereotype.Service

@Service
class DashboardService(
    private val doacaoRepository: DoacaoRepository,
    private val organizacaoRepository: OrganizacaoRepository,
    private val requisicaoDoacaoRepository: RequisicaoDoacaoRepository,
    private val voluntarioOrganizacaoRepository: VoluntarioOrganizacaoRepository,
    private val organizacaoService: OrganizacaoService
) {

    fun publico(): DashboardPublicoDTO {
        val disponiveis = doacaoRepository.findByStatusIn(
            listOf(StatusDoacao.PUBLICADO, StatusDoacao.DISPONIVEL, StatusDoacao.PARCIALMENTE_REIVINDICADO)
        )
        val organizacoes = organizacaoRepository.findAll()

        return DashboardPublicoDTO(
            doacoesDisponiveis = disponiveis.size,
            quantidadeDisponivel = disponiveis.sumOf { quantidadeDisponivel(it) },
            organizacoesDoadoras = organizacoes.count { it.role in rolesDoadoras },
            ongsAtivas = organizacoes.count { it.role == OrganizacaoRole.ONG },
            doacoesConcluidas = doacaoRepository.findByStatusIn(listOf(StatusDoacao.COMPLETO)).size
        )
    }

    fun doador(organizacaoId: Long?, roleToken: String?): DashboardDoadorDTO {
        validarOrganizacao(organizacaoId)
        validarDoador(roleToken)

        val organizacao = organizacaoService.findById(organizacaoId)
        val doacoes = doacaoRepository.findByOrganizacao(organizacao)
        val requisicoes = requisicaoDoacaoRepository.findByDoacaoOrganizacao(organizacao)

        return DashboardDoadorDTO(
            totalDoacoes = doacoes.size,
            doacoesPublicadas = doacoes.count { it.status in listOf(StatusDoacao.PUBLICADO, StatusDoacao.DISPONIVEL) },
            doacoesParciais = doacoes.count { it.status == StatusDoacao.PARCIALMENTE_REIVINDICADO },
            doacoesCompletas = doacoes.count { it.status == StatusDoacao.COMPLETO },
            requisicoesPendentes = requisicoes.count { it.status == StatusReivindicacao.PENDENTE },
            coletasAguardando = requisicoes.count { it.status == StatusReivindicacao.APROVADO },
            coletasConcluidas = requisicoes.count { it.status == StatusReivindicacao.CONCLUIDO },
            quantidadeDoada = requisicoes
                .filter { it.status == StatusReivindicacao.CONCLUIDO }
                .sumOf { it.quantidadeSolicitada }
        )
    }

    fun ong(organizacaoId: Long?, roleToken: String?): DashboardOngDTO {
        validarOrganizacao(organizacaoId)
        if (roleToken != OrganizacaoRole.ONG.name) {
            throw BusinessException("Apenas ONGs podem acessar este dashboard")
        }

        val organizacao = organizacaoService.findById(organizacaoId)
        val requisicoes = requisicaoDoacaoRepository.findByOrganizacaoSolicitante(organizacao)
        val disponiveis = doacaoRepository.findByStatusIn(
            listOf(StatusDoacao.PUBLICADO, StatusDoacao.DISPONIVEL, StatusDoacao.PARCIALMENTE_REIVINDICADO)
        )
        val voluntariosAtivos = voluntarioOrganizacaoRepository
            .findAllByOrganizacaoId(organizacao.id!!)
            .count { it.status == StatusVoluntario.ATIVO }

        return DashboardOngDTO(
            requisicoesPendentes = requisicoes.count { it.status == StatusReivindicacao.PENDENTE },
            requisicoesAprovadas = requisicoes.count { it.status == StatusReivindicacao.APROVADO },
            requisicoesConcluidas = requisicoes.count { it.status == StatusReivindicacao.CONCLUIDO },
            requisicoesCanceladas = requisicoes.count { it.status == StatusReivindicacao.CANCELADO },
            doacoesDisponiveis = disponiveis.size,
            voluntariosAtivos = voluntariosAtivos,
            quantidadeRecebida = requisicoes
                .filter { it.status == StatusReivindicacao.CONCLUIDO }
                .sumOf { it.quantidadeSolicitada }
        )
    }

    private fun validarOrganizacao(organizacaoId: Long?) {
        if (organizacaoId == null || organizacaoId <= 0) {
            throw ValidationException("Organizacao do token e obrigatoria")
        }
    }

    private fun validarDoador(roleToken: String?) {
        if (roleToken !in rolesDoadoras.map { it.name }) {
            throw BusinessException("Apenas doadores, produtores ou distribuidores podem acessar este dashboard")
        }
    }

    private fun quantidadeDisponivel(doacao: Doacao): Double {
        val quantidadeReservada = requisicaoDoacaoRepository
            .findByDoacaoAndStatusIn(
                doacao,
                listOf(StatusReivindicacao.APROVADO, StatusReivindicacao.CONCLUIDO)
            )
            .sumOf { it.quantidadeSolicitada }

        return (doacao.quantidade - quantidadeReservada).coerceAtLeast(0.0)
    }

    private val rolesDoadoras = setOf(
        OrganizacaoRole.DOADOR,
        OrganizacaoRole.PRODUTOR,
        OrganizacaoRole.DISTRIBUIDOR
    )
}
