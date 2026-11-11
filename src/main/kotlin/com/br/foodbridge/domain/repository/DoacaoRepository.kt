package com.br.foodbridge.domain.repository

import com.br.foodbridge.domain.enums.StatusDoacao
import com.br.foodbridge.domain.model.Doacao
import com.br.foodbridge.domain.model.Organizacao
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import jakarta.persistence.LockModeType

@Repository
interface DoacaoRepository : JpaRepository<Doacao, Long> {
    fun findByOrganizacao(organizacao: Organizacao): List<Doacao>
    fun findByStatusIn(status: Collection<StatusDoacao>): List<Doacao>

    /**
     * A linha da doação é o mutex transacional compartilhado por todas as
     * aprovações e alterações de estoque. Requer @Transactional no chamador.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Doacao d where d.id = :id")
    fun findByIdForUpdate(@Param("id") id: Long): Doacao?
}
