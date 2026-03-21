package com.br.foodbridge.controller

import com.br.foodbridge.controller.dto.mapper.OrganizacaoMapper.toResponse
import com.br.foodbridge.controller.dto.organizacao.CreateUpdateOrganizacaoRequest
import com.br.foodbridge.controller.dto.organizacao.OrganizacaoDTO
import com.br.foodbridge.domain.model.Organizacao
import com.br.foodbridge.middleware.RequestContext
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
    private val usuarioService: UsuarioService
) {

    @PostMapping
    fun cadastrarOuVincular(
        @RequestBody request: CreateUpdateOrganizacaoRequest
    ): ResponseEntity<OrganizacaoDTO> {

        val usuario = RequestContext.getUsuario(usuarioService)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val usuarioEntity = usuarioService.findByIdEntity(usuario.id)

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
    fun getMinhaOrganizacao(): ResponseEntity<Organizacao> {
        val usuario = RequestContext.getUsuario(usuarioService)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val vinculo = usuario.organizacoes?.firstOrNull()
        return if (vinculo != null) ResponseEntity.ok(vinculo.organizacao)
        else ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }

    @GetMapping
    fun listarTodas(): ResponseEntity<List<Organizacao>> {
        val organizacoes = organizacaoService.findAll()
        return ResponseEntity.ok(organizacoes)
    }

    @DeleteMapping("/{id}")
    fun deletar(@PathVariable id: Long): ResponseEntity<Void> {
        organizacaoService.delete(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/usuarios/{vinculoId}/aprovar")
    fun aprovarUsuario(
        @PathVariable vinculoId: Long
    ): ResponseEntity<Void> {

        val usuario = RequestContext.getUsuario(usuarioService)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        organizacaoService.aprovarUsuarioOrganizacao(vinculoId, usuario.id!!)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/usuarios/{vinculoId}/reprovar")
    fun reprovarUsuario(
        @PathVariable vinculoId: Long
    ): ResponseEntity<Void> {
        val usuario = RequestContext.getUsuario(usuarioService)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        organizacaoService.reprovarUsuario(vinculoId, usuario.id!!)
        return ResponseEntity.noContent().build()
    }
}