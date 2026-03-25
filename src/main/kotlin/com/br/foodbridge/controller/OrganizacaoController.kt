package com.br.foodbridge.controller

import com.br.foodbridge.controller.dto.mapper.OrganizacaoMapper.toResponse
import com.br.foodbridge.controller.dto.organizacao.CreateUpdateOrganizacaoRequest
import com.br.foodbridge.controller.dto.organizacao.OrganizacaoDTO
import com.br.foodbridge.domain.model.Organizacao
import com.br.foodbridge.controller.dto.auth.TokenData
import org.springframework.security.core.annotation.AuthenticationPrincipal
import com.br.foodbridge.service.OrganizacaoService
import com.br.foodbridge.service.UsuarioService
import com.br.foodbridge.service.utils.JwtService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
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

        val usuarioEntity = usuarioService.findByIdEntity(tokenData.usuarioId)

        val organizacao = organizacaoService
            .cadastrarOuVincularOrganizacao(usuarioEntity, request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(toResponse(organizacao))
    }

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody request: CreateUpdateOrganizacaoRequest
    ): ResponseEntity<Organizacao> {
        val atualizado = organizacaoService.update(id, request)
        return ResponseEntity.ok(atualizado)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<Organizacao> {
        val org = organizacaoService.findById(id)
        return ResponseEntity.ok(org)
    }

    @GetMapping("/me")
    fun getMinhaOrganizacao(
        @AuthenticationPrincipal tokenData: TokenData
    ): ResponseEntity<Organizacao> {
        val usuario = usuarioService.findByIdEntity(tokenData.usuarioId)

        val vinculo = usuario.organizacoes?.firstOrNull { it.organizacao?.id == tokenData.organizacaoId }
        return if (vinculo != null) ResponseEntity.ok(vinculo.organizacao)
        else ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }

    @GetMapping
    fun listarTodas(): ResponseEntity<List<Organizacao>> {
        val organizacoes = organizacaoService.findAll()
        return ResponseEntity.ok(organizacoes)
    }

    // Apagar organização
    @DeleteMapping("/{id}")
    fun deletar(@PathVariable id: Long, @AuthenticationPrincipal tokenData: TokenData,): ResponseEntity<Void> {
        organizacaoService.delete(id, tokenData.usuarioId)
        return ResponseEntity.noContent().build()
    }

    // Aprovar usuário (Que solicitar o vinculo com a mesma organização do usuário ativo)
    @PatchMapping("/usuarios/{vinculoId}/aprovar")
    fun aprovarUsuario(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable vinculoId: Long
    ): ResponseEntity<Void> {

        organizacaoService.aprovarUsuarioOrganizacao(vinculoId, tokenData.usuarioId, tokenData.organizacaoId, tokenData.role)
        return ResponseEntity.noContent().build()
    }

    // Reprovar usuário (Que solicitar o vinculo com a mesma organização do usuário ativo)
    @PatchMapping("/usuarios/{vinculoId}/reprovar")
    fun reprovarUsuario(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable vinculoId: Long
    ): ResponseEntity<Void> {
        organizacaoService.reprovarUsuario(vinculoId, tokenData.usuarioId, tokenData.organizacaoId, tokenData.role)
        return ResponseEntity.noContent().build()
    }
}