package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.mapper.UsuarioMapper
import com.br.foodbridge.controller.dto.organizacao.CreateUpdateOrganizacaoRequest
import com.br.foodbridge.controller.dto.organizacao.OrganizacaoResumoDTO
import com.br.foodbridge.controller.dto.usuario.CreateUpdateUserRequest
import com.br.foodbridge.controller.dto.usuario.UsuarioResponse
import com.br.foodbridge.domain.enums.StatusOrganizacao
import com.br.foodbridge.domain.model.Organizacao
import com.br.foodbridge.domain.model.Usuario
import com.br.foodbridge.domain.model.UsuarioOrganizacao
import com.br.foodbridge.domain.repository.OrganizacaoRepository
import com.br.foodbridge.domain.repository.UsuarioOrganizacaoRepository
import com.br.foodbridge.domain.repository.UsuarioRepository

import com.br.foodbridge.service.utils.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val organizacaoRepository: OrganizacaoRepository,
    private val usuarioOrganizacaoRepository: UsuarioOrganizacaoRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    fun findByIdEntity(userId: Long?): Usuario {
        return usuarioRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Usuário não encontrado") }
    }

    // CREATE usuário
    fun createUser(request: CreateUpdateUserRequest): Usuario {
        if (usuarioRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email já cadastrado")
        }

        val usuario = Usuario(
            nome = request.nome,
            email = request.email,
            senha = passwordEncoder.encode(request.senha)
        )

        return usuarioRepository.save(usuario)
    }

    // UPDATE usuário logado
    fun update(usuarioId: Long, request: CreateUpdateUserRequest): UsuarioResponse {
        val usuarioLogado = findByIdEntity(usuarioId)

        val atualizado = usuarioLogado.copy(
            nome = request.nome ?: usuarioLogado.nome,
            email = request.email ?: usuarioLogado.email,
            senha = if (request.senha.isNotBlank()) passwordEncoder.encode(request.senha) else usuarioLogado.senha
        )

        val saved = usuarioRepository.save(atualizado)
        return UsuarioMapper.toResponse(saved)
    }

    // DELETE usuário logado
    fun delete(usuarioId: Long) {
        val usuarioLogado = findByIdEntity(usuarioId)

        // Remove vínculos antes de deletar
        val vinculos = usuarioOrganizacaoRepository.findAllByUsuarioId(usuarioLogado.id!!)
        usuarioOrganizacaoRepository.deleteAll(vinculos)

        usuarioRepository.delete(usuarioLogado)
    }


    // Listar organizações do usuário logado
    fun listarOrganizacoesDoUsuario(usuarioId: Long): List<OrganizacaoResumoDTO> {
        val vinculacoes = usuarioOrganizacaoRepository.findAllByUsuarioId(usuarioId)

        return vinculacoes.map {
            OrganizacaoResumoDTO(
                organizacaoId = it.organizacao?.id!!,
                nome = it.organizacao?.nome,
                role = it.role,
                status = it.status
            )
        }
    }
}