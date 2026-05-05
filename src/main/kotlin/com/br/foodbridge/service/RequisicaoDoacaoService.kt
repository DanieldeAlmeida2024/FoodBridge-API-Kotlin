package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.requisicao.CreateRequisicaoDoacaoRequest
import com.br.foodbridge.domain.enums.OrganizacaoRole
import com.br.foodbridge.domain.enums.StatusDoacao
import com.br.foodbridge.domain.enums.StatusReivindicacao
import com.br.foodbridge.domain.model.Doacao
import com.br.foodbridge.domain.model.Organizacao
import com.br.foodbridge.domain.model.RequisicaoDoacao
import com.br.foodbridge.domain.repository.RequisicaoDoacaoRepository
import com.br.foodbridge.exception.custom.BusinessException
import com.br.foodbridge.exception.custom.ResourceNotFoundException
import com.br.foodbridge.exception.custom.ValidationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class RequisicaoDoacaoService(
    private val requisicaoDoacaoRepository: RequisicaoDoacaoRepository,
    private val doacaoService: DoacaoService,
    private val organizacaoService: OrganizacaoService
) {

    fun criar(request: CreateRequisicaoDoacaoRequest, organizacaoId: Long?, roleToken: String?): RequisicaoDoacao {
        validarRoleSolicitante(roleToken)

        val organizacaoSolicitante = organizacaoService.findById(organizacaoId)
        validarSolicitanteEhOng(organizacaoSolicitante)

        val doacao = doacaoService.findDoacaoEntityById(request.doacaoId)
        val organizacaoDoadora = doacao.organizacao

        validarDoacaoRequisitavel(doacao)

        if (organizacaoDoadora.id == organizacaoSolicitante.id) {
            throw BusinessException("A organização não pode requisitar a própria doação")
        }

        if (request.quantidadeSolicitada > quantidadeDisponivel(doacao)) {
            throw BusinessException("Quantidade solicitada maior que a disponível na doação")
        }

        val pendenciaDuplicada = requisicaoDoacaoRepository
            .existsByDoacaoAndOrganizacaoSolicitanteAndStatus(
                doacao,
                organizacaoSolicitante,
                StatusReivindicacao.PENDENTE
            )

        if (pendenciaDuplicada) {
            throw BusinessException("Já existe uma requisição pendente para esta doação")
        }

        val requisicao = RequisicaoDoacao(
            doacao = doacao,
            organizacaoSolicitante = organizacaoSolicitante,
            quantidadeSolicitada = request.quantidadeSolicitada,
            observacao = request.observacao,
            status = StatusReivindicacao.PENDENTE,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        return requisicaoDoacaoRepository.save(requisicao)
    }

    fun listarMinhasRequisicoes(organizacaoId: Long?, roleToken: String?): List<RequisicaoDoacao> {
        validarRoleSolicitante(roleToken)
        val organizacao = organizacaoService.findById(organizacaoId)
        validarSolicitanteEhOng(organizacao)
        return requisicaoDoacaoRepository.findByOrganizacaoSolicitante(organizacao)
    }

    fun listarRecebidas(organizacaoId: Long?, roleToken: String?): List<RequisicaoDoacao> {
        validarRoleDoadora(roleToken)
        val organizacao = organizacaoService.findById(organizacaoId)
        return requisicaoDoacaoRepository.findByDoacaoOrganizacao(organizacao)
    }

    fun buscarPorId(id: Long?, organizacaoId: Long?): RequisicaoDoacao {
        val requisicao = findById(id)
        validarAcessoRequisicao(requisicao, organizacaoId)
        return requisicao
    }

    @Transactional
    fun aprovar(id: Long?, organizacaoId: Long?, roleToken: String?): RequisicaoDoacao {
        validarRoleDoadora(roleToken)

        val requisicao = findById(id)
        validarDoadorDaRequisicao(requisicao, organizacaoId)
        validarPendente(requisicao)

        val disponivel = quantidadeDisponivel(requisicao.doacao)
        if (requisicao.quantidadeSolicitada > disponivel) {
            throw BusinessException("A doação não possui mais quantidade disponível para aprovar esta requisição")
        }

        val atualizado = requisicao.copy(
            status = StatusReivindicacao.APROVADO,
            updatedAt = LocalDateTime.now(),
            respondedAt = LocalDateTime.now()
        )

        val salvo = requisicaoDoacaoRepository.save(atualizado)
        doacaoService.atualizarStatusComBaseNasRequisicoes(requisicao.doacao)
        return salvo
    }

    fun rejeitar(id: Long?, organizacaoId: Long?, roleToken: String?): RequisicaoDoacao {
        validarRoleDoadora(roleToken)

        val requisicao = findById(id)
        validarDoadorDaRequisicao(requisicao, organizacaoId)
        validarPendente(requisicao)

        val atualizado = requisicao.copy(
            status = StatusReivindicacao.REJEITADO,
            updatedAt = LocalDateTime.now(),
            respondedAt = LocalDateTime.now()
        )

        return requisicaoDoacaoRepository.save(atualizado)
    }

    fun cancelar(id: Long?, organizacaoId: Long?, roleToken: String?): RequisicaoDoacao {
        validarRoleSolicitante(roleToken)

        val requisicao = findById(id)

        if (requisicao.organizacaoSolicitante.id != organizacaoId) {
            throw BusinessException("A requisição não pertence à organização solicitante")
        }

        validarPendente(requisicao)

        return requisicaoDoacaoRepository.save(
            requisicao.copy(
                status = StatusReivindicacao.CANCELADO,
                updatedAt = LocalDateTime.now(),
                respondedAt = LocalDateTime.now()
            )
        )
    }

    fun somarQuantidadeAprovada(doacao: Doacao): Double {
        return requisicaoDoacaoRepository.findByDoacaoAndStatus(doacao, StatusReivindicacao.APROVADO)
            .sumOf { it.quantidadeSolicitada }
    }

    private fun findById(id: Long?): RequisicaoDoacao {
        if (id == null || id <= 0) {
            throw ValidationException("ID da requisição inválido")
        }

        return requisicaoDoacaoRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Requisição não encontrada") }
    }

    private fun validarSolicitanteEhOng(organizacao: Organizacao) {
        if (organizacao.role != OrganizacaoRole.ONG) {
            throw BusinessException("Apenas ONGs podem requisitar doações")
        }
    }

    private fun validarRoleSolicitante(roleToken: String?) {
        if (roleToken != OrganizacaoRole.ONG.name) {
            throw BusinessException("Apenas ONGs podem executar esta ação")
        }
    }

    private fun validarRoleDoadora(roleToken: String?) {
        val rolesPermitidas = setOf(
            OrganizacaoRole.DOADOR.name,
            OrganizacaoRole.PRODUTOR.name,
            OrganizacaoRole.DISTRIBUIDOR.name
        )

        if (roleToken !in rolesPermitidas) {
            throw BusinessException("Apenas doadores, produtores ou distribuidores podem executar esta ação")
        }
    }

    private fun validarDoacaoRequisitavel(doacao: Doacao) {
        val statusPermitidos = setOf(
            StatusDoacao.PUBLICADO,
            StatusDoacao.DISPONIVEL,
            StatusDoacao.PARCIALMENTE_REIVINDICADO
        )

        if (doacao.status !in statusPermitidos) {
            throw BusinessException("A doação não está disponível para requisição")
        }
    }

    private fun quantidadeDisponivel(doacao: Doacao): Double {
        return doacao.quantidade - somarQuantidadeAprovada(doacao)
    }

    private fun validarPendente(requisicao: RequisicaoDoacao) {
        if (requisicao.status != StatusReivindicacao.PENDENTE) {
            throw BusinessException("Apenas requisições pendentes podem ser alteradas")
        }
    }

    private fun validarDoadorDaRequisicao(requisicao: RequisicaoDoacao, organizacaoId: Long?) {
        val organizacaoDoadoraId = requisicao.doacao.organizacao.id

        if (organizacaoDoadoraId != organizacaoId) {
            throw BusinessException("A requisição não pertence às doações desta organização")
        }
    }

    private fun validarAcessoRequisicao(requisicao: RequisicaoDoacao, organizacaoId: Long?) {
        val organizacaoDoadoraId = requisicao.doacao.organizacao.id
        val organizacaoSolicitanteId = requisicao.organizacaoSolicitante.id

        if (organizacaoId != organizacaoDoadoraId && organizacaoId != organizacaoSolicitanteId) {
            throw BusinessException("A organização não tem acesso a esta requisição")
        }
    }
}
