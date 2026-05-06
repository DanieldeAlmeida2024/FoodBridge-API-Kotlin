package com.br.foodbridge.domain.repository

import com.br.foodbridge.domain.model.VoluntarioOrganizacao
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface VoluntarioOrganizacaoRepository : JpaRepository<VoluntarioOrganizacao, Long> {
    fun findAllByVoluntarioId(voluntarioId: Long): List<VoluntarioOrganizacao>
    fun findAllByOrganizacaoId(organizacaoId: Long): List<VoluntarioOrganizacao>
    fun existsByVoluntarioIdAndOrganizacaoId(
        voluntarioId: Long,
        organizacaoId: Long
    ): Boolean
    fun findByVoluntarioIdAndOrganizacaoId(
        voluntarioId: Long,
        organizacaoId: Long
    ): VoluntarioOrganizacao?
}
