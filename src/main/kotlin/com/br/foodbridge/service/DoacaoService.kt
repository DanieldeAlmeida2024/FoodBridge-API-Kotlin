package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.doacao.DoacaoDTO
import com.br.foodbridge.domain.model.Doacao
import com.br.foodbridge.domain.model.Organizacao
import com.br.foodbridge.domain.repository.DoacaoRepository
import org.springframework.stereotype.Service

@Service
class DoacaoService (
    private val doacaoRepository: DoacaoRepository,
    private val organizacaoService: OrganizacaoService
){

    fun criarDoacao(doacao: DoacaoDTO, organizacao: Organizacao): Doacao {
        val doacao = Doacao(
            tipoComida = doacao.tipoComida,
            descricaoComida = doacao.descricaoComida,
            quantidade = doacao.quantidade,
            unidade = doacao.unidade,
            dataExpiracao = doacao.dataExpiracao,
            status = doacao.status,
            endereco = doacao.endereco,
            organizacao = organizacao
        )

        return doacaoRepository.save(doacao)
    }

    // Listagem de doações pela organizacao
    fun listarDoacoesOrganizacao(organizacao: Organizacao): List<Doacao> {
        return doacaoRepository.findByOrganizacao(organizacao)
    }

    // Atualizacao de doação
    fun editarDoacao(doacao: DoacaoDTO, organizacao: Organizacao):Doacao {
        val doacao = Doacao(
            tipoComida = doacao.tipoComida,
            descricaoComida = doacao.descricaoComida,
            quantidade = doacao.quantidade,
            unidade = doacao.unidade,
            dataExpiracao = doacao.dataExpiracao,
            status = doacao.status,
            endereco = doacao.endereco,
            organizacao = organizacao
        )
        return doacaoRepository.save(doacao)
    }

    // Apagar doacao
    fun deletarDoacao(doacao: Long, organizacao: Organizacao) {
        if(organizacao.id != null){
            throw IllegalArgumentException("Usuário não logado")
        }
        val organizacao = organizacaoService.findById(organizacao.id)
        if (vinculoExistente != null) {
            throw IllegalArgumentException("Usuário já vinculado a esta organização")
        }
        val doacao: Doacao = doacaoRepository.findById(doacao).orElseThrow{
            throw IllegalArgumentException("Doacao nao encontrado")}
        return doacaoRepository.delete(doacao)
    }

    // Listar doação específica
    fun listarDoacao(id: Long): DoacaoDTO {
        val doacao = doacaoRepository.findById(id).orElseThrow {
            throw IllegalArgumentException("Doacao não encontrado")
        }
        val retorno = DoacaoDTO(
            tipoComida = doacao.tipoComida,
            descricaoComida = doacao.descricaoComida,
            quantidade = doacao.quantidade,
            unidade = doacao.unidade,
            dataExpiracao = doacao.dataExpiracao,
            status = doacao.status,
            endereco = doacao.endereco,
            organizacao = doacao.organizacao
        )
        return retorno
    }
}