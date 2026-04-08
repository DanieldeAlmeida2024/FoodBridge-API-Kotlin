package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.doacao.DoacaoDTO
import com.br.foodbridge.domain.model.Doacao
import com.br.foodbridge.domain.model.Organizacao
import com.br.foodbridge.domain.repository.DoacaoRepository
import com.br.foodbridge.exception.custom.BusinessException
import com.br.foodbridge.exception.custom.ResourceNotFoundException
import com.br.foodbridge.exception.custom.ValidationException
import org.springframework.stereotype.Service

@Service
class DoacaoService(
    private val doacaoRepository: DoacaoRepository,
    private val organizacaoService: OrganizacaoService
) {

    fun criarDoacao(request: DoacaoDTO, organizacaoId: Long?): Doacao {

        if (organizacaoId == null || organizacaoId <= 0) {
            throw ValidationException("ID da organização inválido")
        }

        if (request.quantidade <= 0) {
            throw ValidationException("Quantidade deve ser maior que zero")
        }

        val organizacao = organizacaoService.findById(organizacaoId)

        val nova = Doacao(
            tipoComida = request.tipoComida,
            descricaoComida = request.descricaoComida,
            quantidade = request.quantidade,
            unidade = request.unidade,
            dataExpiracao = request.dataExpiracao,
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

        val atualizado = doacao.copy(
            tipoComida = request.tipoComida,
            descricaoComida = request.descricaoComida,
            quantidade = request.quantidade,
            unidade = request.unidade,
            dataExpiracao = request.dataExpiracao,
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
            tipoComida = doacao.tipoComida,
            descricaoComida = doacao.descricaoComida,
            quantidade = doacao.quantidade,
            unidade = doacao.unidade,
            dataExpiracao = doacao.dataExpiracao,
            status = doacao.status,
            endereco = doacao.endereco,
            organizacao = organizacao
        )
    }

    // HELPERS
    private fun findDoacaoById(id: Long?): Doacao {

        if (id == null || id <= 0) {
            throw ValidationException("ID da doação inválido")
        }

        return doacaoRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Doação não encontrada") }
    }
}