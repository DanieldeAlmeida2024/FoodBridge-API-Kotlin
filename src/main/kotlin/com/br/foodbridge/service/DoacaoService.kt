package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.doacao.DoacaoDTO
import com.br.foodbridge.controller.dto.doacao.FecharDoacaoRequest
import com.br.foodbridge.domain.enums.StatusDoacao
import com.br.foodbridge.domain.model.Doacao
import com.br.foodbridge.domain.repository.DoacaoRepository
import com.br.foodbridge.domain.repository.RequisicaoDoacaoRepository
import com.br.foodbridge.domain.enums.StatusReivindicacao
import com.br.foodbridge.exception.custom.BusinessException
import com.br.foodbridge.exception.custom.ResourceNotFoundException
import com.br.foodbridge.exception.custom.ValidationException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class DoacaoService(
    private val doacaoRepository: DoacaoRepository,
    private val organizacaoService: OrganizacaoService,
    private val requisicaoDoacaoRepository: RequisicaoDoacaoRepository
) {

    fun criarDoacao(request: DoacaoDTO, organizacaoId: Long?): Doacao {

        if (organizacaoId == null || organizacaoId <= 0) {
            throw ValidationException("ID da organização inválido")
        }

        if (request.quantidade <= 0) {
            throw ValidationException("Quantidade deve ser maior que zero")
        }

        validarJanelasDisponiveis(request)

        val organizacao = organizacaoService.findById(organizacaoId)

        val nova = Doacao(
            tipoComida = request.tipoComida,
            descricaoComida = request.descricaoComida,
            quantidade = request.quantidade,
            unidade = request.unidade,
            dataExpiracao = request.dataExpiracao,
            janelasDisponiveis = request.janelasDisponiveis,
            status = request.status,
            endereco = request.endereco,
            organizacao = organizacao
        )

        return doacaoRepository.save(nova)
    }

    fun listarDoacoesOrganizacao(organizacaoId: Long?): List<Doacao> {

        if (organizacaoId == null || organizacaoId <= 0) {
            throw ValidationException("ID da organização inválido")
        }

        val organizacao = organizacaoService.findById(organizacaoId)

        return doacaoRepository.findByOrganizacao(organizacao)
    }

    fun listarDoacoesDisponiveis(organizacaoId: Long?): List<Doacao> {
        val statusDisponiveis = listOf(
            StatusDoacao.PUBLICADO,
            StatusDoacao.DISPONIVEL,
            StatusDoacao.PARCIALMENTE_REIVINDICADO
        )

        return doacaoRepository.findByStatusIn(statusDisponiveis)
            .filter { it.organizacao.id != organizacaoId }
    }

    fun editarDoacao(id: Long?, request: DoacaoDTO, organizacaoId: Long?): Doacao {

        if (id == null || id <= 0) {
            throw ValidationException("ID da doação inválido")
        }

        if (organizacaoId == null || organizacaoId <= 0) {
            throw ValidationException("ID da organização inválido")
        }

        val doacao = findDoacaoById(id)

        if (doacao.organizacao?.id != organizacaoId) {
            throw BusinessException("Doação não pertence a esta organização")
        }

        validarJanelasDisponiveis(request)

        val atualizado = doacao.copy(
            tipoComida = request.tipoComida,
            descricaoComida = request.descricaoComida,
            quantidade = request.quantidade,
            unidade = request.unidade,
            dataExpiracao = request.dataExpiracao,
            janelasDisponiveis = request.janelasDisponiveis,
            status = request.status,
            endereco = request.endereco
        )

        return doacaoRepository.save(atualizado)
    }

    fun deletarDoacao(id: Long?, organizacaoId: Long?) {

        if (id == null || id <= 0) {
            throw ValidationException("ID da doação inválido")
        }

        if (organizacaoId == null || organizacaoId <= 0) {
            throw ValidationException("ID da organização inválido")
        }

        val doacao = findDoacaoById(id)

        if (doacao.organizacao?.id != organizacaoId) {
            throw BusinessException("Doação não pertence a esta organização")
        }

        doacaoRepository.delete(doacao)
    }

    fun listarDoacao(id: Long?): DoacaoDTO {

        val doacao = findDoacaoById(id)

        val organizacao = doacao.organizacao
            ?: throw BusinessException("Doação sem organização")

        return DoacaoDTO(
            id = doacao.id,
            tipoComida = doacao.tipoComida,
            descricaoComida = doacao.descricaoComida,
            quantidade = doacao.quantidade,
            unidade = doacao.unidade,
            dataExpiracao = doacao.dataExpiracao,
            janelasDisponiveis = doacao.janelasDisponiveis,
            status = doacao.status,
            endereco = doacao.endereco,
            organizacao = organizacao,
            quantidadeColetadaFinal = doacao.quantidadeColetadaFinal,
            fechadoAt = doacao.fechadoAt,
            observacaoFechamento = doacao.observacaoFechamento
        )
    }

    fun fecharDoacao(id: Long?, request: FecharDoacaoRequest, organizacaoId: Long?): Doacao {

        if (organizacaoId == null || organizacaoId <= 0) {
            throw ValidationException("ID da organização inválido")
        }

        val doacao = findDoacaoById(id)

        if (doacao.organizacao?.id != organizacaoId) {
            throw BusinessException("Doação não pertence a esta organização")
        }

        val statusFechaveis = setOf(
            StatusDoacao.PUBLICADO,
            StatusDoacao.DISPONIVEL,
            StatusDoacao.PARCIALMENTE_REIVINDICADO,
            StatusDoacao.TOTALMENTE_REIVINDICADO
        )

        if (doacao.status !in statusFechaveis) {
            throw BusinessException("A doação não pode ser fechada no status atual: ${doacao.status}")
        }

        if (request.quantidadeColetada > doacao.quantidade) {
            throw ValidationException("Quantidade coletada não pode ser maior que a quantidade total da doação")
        }

        val fechado = doacao.copy(
            status = StatusDoacao.COMPLETO,
            quantidadeColetadaFinal = request.quantidadeColetada,
            fechadoAt = LocalDateTime.now(),
            observacaoFechamento = request.observacao
        )

        return doacaoRepository.save(fechado)
    }

    fun findDoacaoEntityById(id: Long?): Doacao = findDoacaoById(id)

    fun atualizarStatusComBaseNasRequisicoes(doacao: Doacao): Doacao {
        val quantidadeAprovada = requisicaoDoacaoRepository
            .findByDoacaoAndStatusIn(
                doacao,
                listOf(StatusReivindicacao.APROVADO, StatusReivindicacao.CONCLUIDO)
            )
            .sumOf { it.quantidadeSolicitada }

        val quantidadeConcluida = requisicaoDoacaoRepository
            .findByDoacaoAndStatus(doacao, StatusReivindicacao.CONCLUIDO)
            .sumOf { it.quantidadeSolicitada }

        val novoStatus = when {
            quantidadeConcluida >= doacao.quantidade -> StatusDoacao.COMPLETO
            quantidadeAprovada <= 0.0 -> if (doacao.status == StatusDoacao.PUBLICADO) {
                StatusDoacao.PUBLICADO
            } else {
                StatusDoacao.DISPONIVEL
            }
            quantidadeAprovada < doacao.quantidade -> StatusDoacao.PARCIALMENTE_REIVINDICADO
            else -> StatusDoacao.TOTALMENTE_REIVINDICADO
        }

        val atualizado = doacao.copy(status = novoStatus)
        return doacaoRepository.save(atualizado)
    }

    // HELPERS
    private fun findDoacaoById(id: Long?): Doacao {

        if (id == null || id <= 0) {
            throw ValidationException("ID da doação inválido")
        }

        return doacaoRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Doação não encontrada") }
    }

    private fun validarJanelasDisponiveis(request: DoacaoDTO) {
        if (request.janelasDisponiveis.isEmpty()) {
            throw ValidationException("Informe ao menos uma janela de horario disponivel para coleta")
        }

        val janelaInvalida = request.janelasDisponiveis.any { it.fim <= it.inicio }
        if (janelaInvalida) {
            throw ValidationException("A janela de horario deve terminar apos o inicio")
        }
    }
}
