package com.br.foodbridge.domain.repository

import com.br.foodbridge.domain.model.Usuario
import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

@Repository
interface UsuarioRepository : JpaRepository<Usuario, Long> {
    fun findByEmail(email: String): Usuario?
    fun existsByEmail(email: String): Boolean
    fun findById(id: Long?): Optional<Usuario>
}