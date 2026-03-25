package com.br.foodbridge.domain.repository

import com.br.foodbridge.controller.dto.doacao.DoacaoDTO
import com.br.foodbridge.domain.model.Doacao
import com.br.foodbridge.domain.model.Organizacao
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DoacaoRepository : JpaRepository<Doacao, Long> {
    fun findByOrganizacao(organizacao: Organizacao): List<Doacao>
}