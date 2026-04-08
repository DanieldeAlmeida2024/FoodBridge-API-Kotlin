package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.organizacao.CreateUpdateOrganizacaoRequest
import com.br.foodbridge.domain.enums.OrganizacaoRole
import com.br.foodbridge.domain.enums.StatusOrganizacao
import com.br.foodbridge.domain.model.Endereco
import com.br.foodbridge.domain.model.Organizacao
import com.br.foodbridge.domain.model.Usuario
import com.br.foodbridge.domain.model.UsuarioOrganizacao
import com.br.foodbridge.domain.repository.OrganizacaoRepository
import com.br.foodbridge.domain.repository.UsuarioOrganizacaoRepository
import com.br.foodbridge.exception.custom.BusinessException
import com.br.foodbridge.exception.custom.ValidationException
import com.br.foodbridge.exception.custom.ResourceNotFoundException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrganizacaoService(
    private val organizacaoRepository: OrganizacaoRepository,
    private val usuarioOrganizacaoRepository: UsuarioOrganizacaoRepository
) {

    fun aprovarUsuarioOrganizacao(
        vinculoId: Long,
        organizacaoIdToken: Long?,
        roleToken: String?
    ) {
        val vinculo = getVinculoOrThrow(vinculoId)

        val organizacaoId = vinculo.organizacao?.id
            ?: throw BusinessException("Vínculo inválido sem organização")

        validarAcessoAprovacao(organizacaoId, organizacaoIdToken, roleToken)

        val atualizado = vinculo.copy(
            status = StatusOrganizacao.VERIFICADO,
            approvedAt = LocalDateTime.now()
        )

        usuarioOrganizacaoRepository.save(atualizado)
    }

    fun reprovarUsuario(
        vinculoId: Long,
        organizacaoIdToken: Long?,
        roleToken: String?
    ) {
        val vinculo = getVinculoOrThrow(vinculoId)

        val organizacaoId = vinculo.organizacao?.id
            ?: throw BusinessException("Vínculo inválido sem organização")

        validarAcessoAprovacao(organizacaoId, organizacaoIdToken, roleToken)

        val atualizado = vinculo.copy(
            status = StatusOrganizacao.INATIVO
        )

        usuarioOrganizacaoRepository.save(atualizado)
    }

    fun findByCnpj(cnpj: String?): Organizacao? {
        if (cnpj.isNullOrBlank()) {
            throw ValidationException("CNPJ é obrigatório")
        }

        return organizacaoRepository.findByCnpj(cnpj)
    }

    fun cadastrarOuVincularOrganizacao(
        usuario: Usuario,
        request: CreateUpdateOrganizacaoRequest
    ): Organizacao {

        if (usuario.id == null) {
            throw BusinessException("Usuário inválido")
        }

        if (request.cnpj.isBlank()) {
            throw ValidationException("CNPJ é obrigatório")
        }

        if (request.nome.isBlank()) {
            throw ValidationException("Nome da organização é obrigatório")
        }

        val organizacao = organizacaoRepository.findByCnpj(request.cnpj)
            ?: criarOrganizacao(request)

        val vinculoExistente = usuarioOrganizacaoRepository
            .findByUsuarioIdAndOrganizacaoId(usuario.id, organizacao.id!!)

        if (vinculoExistente != null) {
            throw BusinessException("Usuário já vinculado a esta organização")
        }

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

        val endereco = Endereco(
            linha1 = request.endereco.linha1,
            linha2 = request.endereco.linha2,
            numero = request.endereco.numero,
            cep = request.endereco.cep,
            bairro = request.endereco.bairro,
            cidade = request.endereco.cidade,
            estado = request.endereco.estado,
            pais = request.endereco.pais
        )

        val nova = Organizacao(
            nome = request.nome,
            cnpj = request.cnpj,
            description = request.description,
            telefone = request.telefone,
            email = request.email,
            website = request.website,
            status = StatusOrganizacao.REVISAO,
            role = request.role,
            createdAt = LocalDateTime.now(),
            endereco = endereco
        )

        return organizacaoRepository.save(nova)
    }

    fun findById(id: Long?): Organizacao {

        if (id == null || id <= 0) {
            throw ValidationException("ID da organização inválido")
        }

        return organizacaoRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Organização não encontrada") }
    }

    fun findAll(): List<Organizacao> = organizacaoRepository.findAll()

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

    fun inativarOrganizacao(id: Long) {

        val org = findById(id)

        val atualizado = org.copy(
            status = StatusOrganizacao.INATIVO
        )

        organizacaoRepository.save(atualizado)
    }

    // HELPERS
    private fun getVinculoOrThrow(id: Long): UsuarioOrganizacao {

        if (id <= 0) {
            throw ValidationException("ID do vínculo inválido")
        }

        return usuarioOrganizacaoRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Vínculo não encontrado") }
    }

    private fun validarAcessoAprovacao(
        organizacaoIdAlvo: Long,
        organizacaoIdToken: Long?,
        roleToken: String?
    ) {

        if (organizacaoIdToken == null) {
            throw ValidationException("Organização do token é obrigatória")
        }

        if (organizacaoIdToken != organizacaoIdAlvo) {
            throw BusinessException("Usuário não pertence à organização")
        }

        if (roleToken != OrganizacaoRole.ADMIN.name) {
            throw BusinessException("Apenas administradores podem realizar essa ação")
        }
    }
}