package com.br.foodbridge.domain.repository

import com.br.foodbridge.domain.enums.StatusReivindicacao
import com.br.foodbridge.domain.model.Doacao
import com.br.foodbridge.domain.model.Organizacao
import com.br.foodbridge.domain.model.RequisicaoDoacao
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RequisicaoDoacaoRepository : JpaRepository<RequisicaoDoacao, Long> {
    fun findByOrganizacaoSolicitante(organizacao: Organizacao): List<RequisicaoDoacao>
    fun findByDoacaoOrganizacao(organizacao: Organizacao): List<RequisicaoDoacao>
    fun findByDoacao(doacao: Doacao): List<RequisicaoDoacao>
    fun findByDoacaoAndStatus(doacao: Doacao, status: StatusReivindicacao): List<RequisicaoDoacao>
    fun findByDoacaoAndStatusIn(doacao: Doacao, status: Collection<StatusReivindicacao>): List<RequisicaoDoacao>
    fun findByOrganizacaoSolicitanteAndStatusIn(
        organizacao: Organizacao,
        status: Collection<StatusReivindicacao>
    ): List<RequisicaoDoacao>
    fun existsByDoacaoAndOrganizacaoSolicitanteAndStatus(
        doacao: Doacao,
        organizacaoSolicitante: Organizacao,
        status: StatusReivindicacao
    ): Boolean
}
