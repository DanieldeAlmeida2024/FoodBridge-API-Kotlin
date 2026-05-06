package com.br.foodbridge.domain.repository

import com.br.foodbridge.domain.model.Voluntario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VoluntarioRepository : JpaRepository<Voluntario, Long> {
    fun findByEmail(email: String): Voluntario?
    fun existsByCpf(cpf: String): Boolean
    fun findByCpf(cpf: String): Voluntario?
    fun existsByEmail(email: String): Boolean
}
