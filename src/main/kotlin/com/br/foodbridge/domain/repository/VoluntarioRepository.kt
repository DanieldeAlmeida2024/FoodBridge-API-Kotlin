package com.br.foodbridge.domain.repository

import com.br.foodbridge.controller.dto.voluntario.CreateUpdateVoluntario
import com.br.foodbridge.domain.model.Voluntario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface VoluntarioRepository : JpaRepository<Voluntario, Long> {
    fun findByEmail(email: String): Voluntario?
    fun existsByCpf(cpf: String): Boolean
    fun findByCpf(cpf: String): Voluntario
    fun existsByEmail(email: String): Boolean
    fun save(entity: com.br.foodbridge.controller.dto.voluntario.CreateUpdateVoluntario): com.br.foodbridge.domain.model.Voluntario
}