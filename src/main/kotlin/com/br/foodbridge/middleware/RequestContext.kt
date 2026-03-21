package com.br.foodbridge.middleware

import com.br.foodbridge.domain.enums.OrganizacaoRole
import com.br.foodbridge.domain.model.Usuario
import com.br.foodbridge.service.UsuarioService

object RequestContext {

    private val context: ThreadLocal<UserContext> = ThreadLocal()

    data class UserContext(
        val usuarioId: Long,
        val organizacaoId: Long?,
        val role: String?
    )

    fun set(usuarioId: Long, organizacaoId: Long?, role: String?) {
        context.set(UserContext(usuarioId, organizacaoId, role))
    }

    fun clear() {
        context.remove()
    }

    fun getUserId(): Long? = context.get()?.usuarioId
    fun getOrganizacaoId(): Long? = context.get()?.organizacaoId
    fun getRole(): String? = context.get()?.role
    fun isAdmin(): Boolean = context.get()?.role == OrganizacaoRole.ADMIN.name

    /**
     * 🔹 Retorna o usuário completo via UsuarioService
     */
    fun getUsuario(usuarioService: UsuarioService): Usuario? {
        val userId = getUserId() ?: return null
        return usuarioService.findByIdEntity(userId)
    }
}