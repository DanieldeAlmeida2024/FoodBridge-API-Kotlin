package com.br.foodbridge.controller

import com.br.foodbridge.controller.dto.mapper.OrganizacaoMapper.toResponse
import com.br.foodbridge.controller.dto.organizacao.CreateUpdateOrganizacaoRequest
import com.br.foodbridge.controller.dto.organizacao.OrganizacaoDTO
import com.br.foodbridge.domain.model.Organizacao
import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.exception.custom.BusinessException
import com.br.foodbridge.exception.custom.ResourceNotFoundException
import com.br.foodbridge.exception.custom.ValidationException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import com.br.foodbridge.service.OrganizacaoService
import com.br.foodbridge.service.UsuarioService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/organizacoes")
class OrganizacaoController(
    private val organizacaoService: OrganizacaoService,
    private val usuarioService: UsuarioService,
) {

    @PostMapping
    fun cadastrarOuVincular(
        @AuthenticationPrincipal tokenData: TokenData,
        @RequestBody request: CreateUpdateOrganizacaoRequest
    ): ResponseEntity<OrganizacaoDTO> {

        val usuarioId = tokenData.usuarioId
            ?: throw ValidationException("Usuário inválido no token")

        val usuario = usuarioService.findByIdEntity(usuarioId)

        val organizacao = organizacaoService
            .cadastrarOuVincularOrganizacao(usuario, request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(toResponse(organizacao))
    }

    @GetMapping("/cnpj/{cnpj}")
    fun buscarPorCnpj(
        @PathVariable cnpj: String
    ): ResponseEntity<OrganizacaoDTO> {

        val org = organizacaoService.findByCnpj(cnpj)
            ?: throw ResourceNotFoundException("Organização não encontrada")

        return ResponseEntity.ok(toResponse(org))
    }

    @GetMapping("/{id}")
    fun buscarPorId(
        @PathVariable id: Long
    ): ResponseEntity<OrganizacaoDTO> {

        val org = organizacaoService.findById(id)

        return ResponseEntity.ok(toResponse(org))
    }

    @GetMapping
    fun listarTodas(): ResponseEntity<List<OrganizacaoDTO>> {

        val lista = organizacaoService.findAll()
            .map { toResponse(it) }

        return ResponseEntity.ok(lista)
    }

    @GetMapping("/me")
    fun minhaOrganizacao(
        @AuthenticationPrincipal tokenData: TokenData
    ): ResponseEntity<OrganizacaoDTO> {

        val organizacaoId = tokenData.organizacaoId
            ?: throw BusinessException("Usuário não vinculado a uma organização")

        val org = organizacaoService.findById(organizacaoId)

        return ResponseEntity.ok(toResponse(org))
    }

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody request: CreateUpdateOrganizacaoRequest
    ): ResponseEntity<OrganizacaoDTO> {

        val atualizado = organizacaoService.update(id, request)

        return ResponseEntity.ok(toResponse(atualizado))
    }

    @DeleteMapping("/{id}")
    fun deletar(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable id: Long
    ): ResponseEntity<Void> {

        if (tokenData.role != "ADMIN") {
            throw BusinessException("Apenas administradores podem inativar a organização")
        }

        organizacaoService.inativarOrganizacao(id)

        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/usuarios/{vinculoId}/aprovar")
    fun aprovarUsuario(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable vinculoId: Long
    ): ResponseEntity<Void> {

        organizacaoService.aprovarUsuarioOrganizacao(
            vinculoId,
            tokenData.organizacaoId,
            tokenData.role
        )

        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/usuarios/{vinculoId}/reprovar")
    fun reprovarUsuario(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable vinculoId: Long
    ): ResponseEntity<Void> {

        organizacaoService.reprovarUsuario(
            vinculoId,
            tokenData.organizacaoId,
            tokenData.role
        )

        return ResponseEntity.noContent().build()
    }

    // ======================
    // MAPPER
    // ======================

    private fun toResponse(org: Organizacao): OrganizacaoDTO {

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
            endereco = org.endereco
        )
    }
}