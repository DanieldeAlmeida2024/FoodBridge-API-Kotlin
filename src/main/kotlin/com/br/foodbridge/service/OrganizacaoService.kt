package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.organizacao.CreateUpdateOrganizacaoRequest
import com.br.foodbridge.controller.dto.organizacao.OrganizacaoDTO
import com.br.foodbridge.domain.enums.OrganizacaoRole
import com.br.foodbridge.domain.enums.StatusOrganizacao
import com.br.foodbridge.domain.model.Organizacao
import com.br.foodbridge.domain.model.Usuario
import com.br.foodbridge.domain.model.UsuarioOrganizacao
import com.br.foodbridge.domain.repository.OrganizacaoRepository
import com.br.foodbridge.domain.repository.UsuarioOrganizacaoRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrganizacaoService(
    private val organizacaoRepository: OrganizacaoRepository,
    private val usuarioOrganizacaoRepository: UsuarioOrganizacaoRepository
) {

    // =========================================================
    // 🔹 APROVAÇÃO / REPROVAÇÃO
    // =========================================================

    fun aprovarUsuarioOrganizacao(vinculoId: Long, usuarioId: Long, organizacaoId: Long?, role: String?) {
        val vinculo = getVinculoOrThrow(vinculoId)

        validarAcessoAprovacao(vinculo.organizacao?.id!!, organizacaoId, role)

        vinculo.status = StatusOrganizacao.VERIFICADO
        vinculo.approvedAt = LocalDateTime.now()

        usuarioOrganizacaoRepository.save(vinculo)
    }

    fun reprovarUsuario(vinculoId: Long, usuarioId: Long, organizacaoId: Long?, role: String?) {
        val vinculo = getVinculoOrThrow(vinculoId)

        validarAcessoAprovacao(vinculo.organizacao?.id!!, organizacaoId, role)

        vinculo.status = StatusOrganizacao.INATIVO

        usuarioOrganizacaoRepository.save(vinculo)
    }

    // =========================================================
    // 🔹 CRIAR OU VINCULAR ORGANIZAÇÃO
    // =========================================================

    fun cadastrarOuVincularOrganizacao(
        usuario: Usuario,
        request: CreateUpdateOrganizacaoRequest
    ): Organizacao {

        // 1️⃣ Busca ou cria organização
        val organizacao = organizacaoRepository.findByCnpj(request.cnpj)
            ?: criarOrganizacao(request)

        // 2️⃣ Verifica se já existe vínculo
        val vinculoExistente = usuarioOrganizacaoRepository
            .findByUsuarioIdAndOrganizacaoId(usuario.id!!, organizacao.id!!)

        if (vinculoExistente != null) {
            throw IllegalArgumentException("Usuário já vinculado a esta organização")
        }

        // 3️⃣ Cria vínculo
        val vinculo = UsuarioOrganizacao(
            usuario = usuario,
            organizacao = organizacao,
            role = request.role,
            status = StatusOrganizacao.VERIFICADO,
            createdAt = LocalDateTime.now()
        )

        usuarioOrganizacaoRepository.save(vinculo)

        return organizacao
    }

    private fun criarOrganizacao(request: CreateUpdateOrganizacaoRequest): Organizacao {
        val nova = Organizacao(
            nome = request.nome,
            cnpj = request.cnpj,
            description = request.description,
            telefone = request.telefone,
            email = request.email,
            website = request.website,
            status = StatusOrganizacao.REVISAO,
            role = request.role,
            createdAt = LocalDateTime.now()
        )

        return organizacaoRepository.save(nova)
    }

    // =========================================================
    // 🔹 CONSULTAS
    // =========================================================

    fun findById(id: Long): Organizacao =
        organizacaoRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Organização não encontrada") }

    fun findAll(): List<Organizacao> =
        organizacaoRepository.findAll()

    // =========================================================
    // 🔹 UPDATE
    // =========================================================

    fun update(id: Long, request: CreateUpdateOrganizacaoRequest): Organizacao {
        val org = findById(id)

        val atualizado = org.copy(
            nome = request.nome,
            description = request.description,
            telefone = request.telefone,
            email = request.email,
            website = request.website
        )

        return organizacaoRepository.save(atualizado)
    }

    // =========================================================
    // 🔹 DELETE
    // =========================================================

    fun delete(id: Long) {
        val org = findById(id)
        organizacaoRepository.delete(org)
    }

    // =========================================================
    // 🔹 HELPERS PRIVADOS
    // =========================================================

    private fun getVinculoOrThrow(id: Long): UsuarioOrganizacao =
        usuarioOrganizacaoRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Vínculo não encontrado") }

    private fun validarAcessoAprovacao(
        organizacaoIdAlvo: Long,
        organizacaoIdToken: Long?,
        roleToken: String?
    ) {
        if (organizacaoIdToken != organizacaoIdAlvo) {
            throw IllegalArgumentException("Usuário não pertence à organização")
        }

        if (roleToken != OrganizacaoRole.ADMIN.name) {
            throw IllegalArgumentException("Apenas administradores podem realizar essa ação")
        }
    }
}