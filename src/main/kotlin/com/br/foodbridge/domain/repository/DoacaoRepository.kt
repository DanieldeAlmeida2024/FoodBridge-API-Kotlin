package com.br.foodbridge.domain.repository

import com.br.foodbridge.domain.enums.StatusDoacao
import com.br.foodbridge.domain.model.Doacao
import com.br.foodbridge.domain.model.Organizacao
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DoacaoRepository : JpaRepository<Doacao, Long> {
    fun findByOrganizacao(organizacao: Organizacao): List<Doacao>
    fun findByStatusIn(status: Collection<StatusDoacao>): List<Doacao>
}
