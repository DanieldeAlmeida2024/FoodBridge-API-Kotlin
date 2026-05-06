package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.organizacao.OrganizacaoDTO
import com.br.foodbridge.controller.dto.usuario.UsuarioDTO
import com.br.foodbridge.domain.enums.*
import com.br.foodbridge.domain.model.Organizacao
import com.br.foodbridge.domain.model.Usuario
import com.br.foodbridge.domain.repository.*
import com.br.foodbridge.exception.custom.BusinessException
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import com.br.foodbridge.exception.custom.ResourceNotFoundException
import com.br.foodbridge.exception.custom.ValidationException

@Service
class AdminService(
    private val organizacaoRepository: OrganizacaoRepository,
    private val usuarioRepository: UsuarioRepository,
) {


    fun listarOrganizacoes(): List<OrganizacaoDTO> =
        organizacaoRepository.findAll().map { mapToOrganizacaoDTO(it) }

    fun listarOrganizacoesPendentes(): List<OrganizacaoDTO> =
        organizacaoRepository.findAllByStatusIn(
            listOf(
                StatusOrganizacao.DOCUMENTOS_PENDENTES,
                StatusOrganizacao.REVISAO
            )
        ).map(::mapToOrganizacaoDTO)

    fun aprovarOrganizacao(id: Long) {

        val org = findOrganizacaoById(id)

        val atualizado = org.copy(
            status = StatusOrganizacao.VERIFICADO
        )

        organizacaoRepository.save(atualizado)
    }

    fun reprovarOrganizacao(id: Long) {

        val org = findOrganizacaoById(id)

        val atualizado = org.copy(
            status = StatusOrganizacao.INATIVO
        )

        organizacaoRepository.save(atualizado)
    }

    fun listarUsuarios(): List<UsuarioDTO> =
        usuarioRepository.findAll().map { mapToUsuarioDTO(it) }

    fun listarUsuariosPendentes(): List<UsuarioDTO> =
        usuarioRepository.findAll()
            .filter { it.status == UserStatus.PENDENTE_VERIFICACAO }
            .map { mapToUsuarioDTO(it) }

    fun aprovarUsuario(userId: Long, aprovadorId: Long?): Usuario {

        if (aprovadorId == null || aprovadorId <= 0) {
            throw ValidationException("ID do aprovador inválido")
        }

        val usuario = findUsuarioById(userId)

        val atualizado = usuario.copy(
            status = UserStatus.VERIFICADO,
            approvedAt = LocalDateTime.now()
        )

        return usuarioRepository.save(atualizado)
    }

    fun reprovarUsuario(userId: Long, reprovadorId: Long?): Usuario {

        if (reprovadorId == null || reprovadorId <= 0) {
            throw ValidationException("ID do reprovador inválido")
        }

        val usuario = findUsuarioById(userId)

        val atualizado = usuario.copy(
            status = UserStatus.INATIVO,
            approvedAt = null
        )

        return usuarioRepository.save(atualizado)
    }

    private fun findOrganizacaoById(id: Long?): Organizacao {

        if (id == null || id <= 0) {
            throw ValidationException("ID da organização inválido")
        }

        return organizacaoRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Organização não encontrada") }
    }

    private fun findUsuarioById(id: Long?): Usuario {

        if (id == null || id <= 0) {
            throw ValidationException("ID do usuário inválido")
        }

        return usuarioRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Usuário não encontrado") }
    }

    private fun mapToOrganizacaoDTO(org: Organizacao): OrganizacaoDTO {

        val id = org.id
            ?: throw BusinessException("Organização sem ID")

        return OrganizacaoDTO(
            id = id,
            nome = org.nome,
            status = org.status,
            usuarios = org.usuarios,
            cnpj = org.cnpj,
            email = org.email,
            telefone = org.telefone,
            voluntarios = org.voluntarios,
            endereco = org.endereco,
            role = org.role
        )
    }

    private fun mapToUsuarioDTO(usuario: Usuario): UsuarioDTO {

        val id = usuario.id
            ?: throw BusinessException("Usuário sem ID")

        return UsuarioDTO(
            id = id,
            nome = usuario.nome,
            email = usuario.email,
            status = usuario.status
        )
    }
}