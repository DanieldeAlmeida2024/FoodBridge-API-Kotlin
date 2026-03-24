package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.organizacao.OrganizacaoDTO
import com.br.foodbridge.controller.dto.usuario.UsuarioDTO
import com.br.foodbridge.controller.dto.usuario.UsuarioOrganizacaoDTO
import com.br.foodbridge.domain.enums.*
import com.br.foodbridge.domain.model.Usuario
import com.br.foodbridge.domain.repository.*
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AdminService(
    private val organizacaoRepository: OrganizacaoRepository,
    private val usuarioRepository: UsuarioRepository,
) {
    // LISTAR ORGANIZAÇÕES
// Listagens
    fun listarOrganizacoes(): List<OrganizacaoDTO> = organizacaoRepository.findAll().map{
        OrganizacaoDTO(
            id = it.id,
            nome = it.nome,
            status = it.status,
            usuarios = it.usuarios,
            cnpj = it.cnpj,
            email = it.email,
            telefone = it.telefone,
        )
    }


    fun listarOrganizacoesPendentes(): List<OrganizacaoDTO> {
        return organizacaoRepository.findAll().map {
            OrganizacaoDTO(
                id = it.id!!,
                nome = it.nome,
                status = it.status,
                usuarios = it.usuarios,
                cnpj = it.cnpj,
                email = it.email,
                telefone = it.telefone,
            )
        }
            .filter { it.status == StatusOrganizacao.DOCUMENTOS_PENDENTES }
    }

    // LISTAR USUÁRIOS (VÍNCULOS)
    fun listarUsuarios(): List<UsuarioDTO> {
        return usuarioRepository.findAll().map {
            UsuarioDTO(
                id = it.id,
                nome = it.nome,
                email = it.email,
                status = it.status
            )}
    }


    fun listarUsuariosPendentes(): List<UsuarioDTO> {
        return usuarioRepository.findAll().map{
            UsuarioDTO(
            id = it.id!!,
            nome = it.nome,
            email = it.email,
            status = it.status,
        )}
            .filter { it.status == UserStatus.PENDENTE_VERIFICACAO }
    }

    // Aprovar ou reprovar organização (só admin)
    fun aprovarOrganizacao(id: Long) {
        val org = organizacaoRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Organização não encontrada") }
        val updated = org.copy(status = StatusOrganizacao.VERIFICADO)
        organizacaoRepository.save(updated)
    }

    fun reprovarOrganizacao(id: Long) {
        val org = organizacaoRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Organização não encontrada") }
        val updated = org.copy(status = StatusOrganizacao.INATIVO)
        organizacaoRepository.save(updated)
    }

    fun aprovarUsuario(userId: Long, aprovadorId: Long): Usuario? {
        val usuario = usuarioRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Usuario não encontrado") }

        usuario.status = UserStatus.VERIFICADO;
        usuario.approvedAt = LocalDateTime.now()

        usuarioRepository.save(usuario)
        return usuario
    }

    fun reprovarUsuario(userId: Long, reprovadorId: Long): Usuario? {
        println("reprovar Usuario "+userId)
        val usuario = usuarioRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Usuário não encontrado") }
        usuario.approvedAt = null;
        usuario.status = UserStatus.INATIVO;

        usuarioRepository.save(usuario)
        return usuario
    }

}