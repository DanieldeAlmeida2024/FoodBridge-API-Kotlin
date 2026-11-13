package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.domain.enums.StatusOrganizacao
import com.br.foodbridge.domain.enums.UserStatus
import com.br.foodbridge.domain.repository.UsuarioOrganizacaoRepository
import com.br.foodbridge.domain.repository.UsuarioRepository
import com.br.foodbridge.service.utils.JwtService
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** Revalida o contexto vigente; a assinatura de um JWT não comprova vínculo ativo. */
@Service
class ContextoAcessoService(
    private val usuarios: UsuarioRepository,
    private val vinculos: UsuarioOrganizacaoRepository
) {
    @Transactional(readOnly = true)
    fun validar(tipo: String, token: TokenData) {
        val usuario = usuarios.findById(token.usuarioId).orElseThrow {
            AccessDeniedException("Sessão sem usuário vigente")
        }
        if (usuario.status == UserStatus.SUSPENSO || usuario.status == UserStatus.INATIVO) {
            throw AccessDeniedException("Conta indisponível")
        }
        if (tipo == JwtService.TOKEN_TYPE_TEMP) return
        if (tipo != JwtService.TOKEN_TYPE_ACCESS || usuario.status != UserStatus.VERIFICADO) {
            throw AccessDeniedException("Contexto de acesso inválido")
        }
        val orgId = token.organizacaoId ?: throw AccessDeniedException("Organização não informada")
        val vinculo = vinculos.findByUsuarioIdAndOrganizacaoId(token.usuarioId, orgId)
            ?: throw AccessDeniedException("Vínculo não vigente")
        if (vinculo.id != token.vinculoId ||
            vinculo.role.name != token.role ||
            vinculo.status != StatusOrganizacao.VERIFICADO ||
            vinculo.organizacao?.status != StatusOrganizacao.VERIFICADO) {
            throw AccessDeniedException("Vínculo não vigente")
        }
    }
}
