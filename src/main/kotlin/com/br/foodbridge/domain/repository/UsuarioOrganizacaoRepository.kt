package com.br.foodbridge.domain.repository

import com.br.foodbridge.domain.model.UsuarioOrganizacao
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UsuarioOrganizacaoRepository : JpaRepository<UsuarioOrganizacao, Long> {
    fun findByUsuarioIdAndOrganizacaoId(
        usuarioId: Long,
        organizacaoId: Long
    ): UsuarioOrganizacao?
    fun findAllByUsuarioId(usuarioId: Long): List<UsuarioOrganizacao>
    fun findAllByOrganizacaoId(organizacaoId: Long): List<UsuarioOrganizacao>
    fun existsByUsuarioIdAndOrganizacaoId(
        usuarioId: Long,
        organizacaoId: Long
    ): Boolean
}