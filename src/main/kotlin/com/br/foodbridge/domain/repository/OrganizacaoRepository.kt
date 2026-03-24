package com.br.foodbridge.domain.repository

import com.br.foodbridge.domain.model.Organizacao
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface OrganizacaoRepository : JpaRepository<Organizacao, Long> {
    fun findByCnpj(cnpj: String): Organizacao?
    fun existsByCnpj(cnpj: String): Boolean
    fun findByEmail(email: String): Organizacao?
}