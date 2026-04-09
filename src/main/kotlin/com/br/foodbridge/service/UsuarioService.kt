package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.mapper.UsuarioMapper
import com.br.foodbridge.controller.dto.organizacao.OrganizacaoResumoDTO
import com.br.foodbridge.controller.dto.usuario.CreateUpdateUserRequest
import com.br.foodbridge.controller.dto.usuario.UsuarioResponse
import com.br.foodbridge.domain.model.Usuario
import com.br.foodbridge.domain.repository.OrganizacaoRepository
import com.br.foodbridge.domain.repository.UsuarioOrganizacaoRepository
import com.br.foodbridge.domain.repository.UsuarioRepository
import com.br.foodbridge.exception.custom.BusinessException
import com.br.foodbridge.exception.custom.ValidationException
import com.br.foodbridge.exception.custom.ResourceNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service



@Service
class UsuarioService(
    private val usuarioRepository: UsuarioRepository,
    private val usuarioOrganizacaoRepository: UsuarioOrganizacaoRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    fun findByIdEntity(userId: Long?): Usuario {
        if (userId == null) {
            throw ValidationException("ID do usuário é obrigatório")
        }

        return usuarioRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("Usuário não encontrado") }
    }

    fun createUser(request: CreateUpdateUserRequest): Usuario {

        if (request.email.isBlank()) {
            throw ValidationException("Email é obrigatório")
        }

        if (request.senha.isBlank()) {
            throw ValidationException("Senha é obrigatória")
        }

        if (usuarioRepository.existsByEmail(request.email)) {
            throw BusinessException("Email já cadastrado")
        }

        val usuario = Usuario(
            nome = request.nome,
            email = request.email,
            senha = passwordEncoder.encode(request.senha)
        )
        return usuarioRepository.save(usuario)
    }

    fun update(usuarioId: Long, request: CreateUpdateUserRequest): UsuarioResponse {

        val usuario = findByIdEntity(usuarioId)

        if (!request.email.isNullOrBlank() &&
            request.email != usuario.email &&
            usuarioRepository.existsByEmail(request.email)
        ) {
            throw BusinessException("Email já cadastrado")
        }

        val senhaAtualizada = when {
            request.senha.isNullOrBlank() -> usuario.senha
            else -> passwordEncoder.encode(request.senha)
        }

        val atualizado = usuario.copy(
            nome = request.nome ?: usuario.nome,
            email = request.email ?: usuario.email,
            senha = senhaAtualizada
        )

        val saved = usuarioRepository.save(atualizado)

        return UsuarioMapper.toResponse(saved)
    }

    fun delete(usuarioId: Long) {

        val usuario = findByIdEntity(usuarioId)

        val vinculos = usuarioOrganizacaoRepository.findAllByUsuarioId(usuario.id!!)

        if (vinculos.isNotEmpty()) {
            usuarioOrganizacaoRepository.deleteAll(vinculos)
        }

        usuarioRepository.delete(usuario)
    }


    fun listarOrganizacoesDoUsuario(usuarioId: Long): List<OrganizacaoResumoDTO> {

        if (usuarioId <= 0) {
            throw ValidationException("ID do usuário inválido")
        }

        val vinculacoes = usuarioOrganizacaoRepository.findAllByUsuarioId(usuarioId)

        return vinculacoes.map { vinculacao ->

            val organizacao = vinculacao.organizacao
                ?: throw BusinessException("Vínculo com organização inválido")

            OrganizacaoResumoDTO(
                organizacaoId = organizacao.id
                    ?: throw BusinessException("Organização sem ID"),
                nome = organizacao.nome,
                role = vinculacao.role,
                status = vinculacao.status
            )
        }
    }
}