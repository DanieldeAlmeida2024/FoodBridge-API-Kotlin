package com.br.foodbridge.controller

import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.controller.dto.doacao.DoacaoDTO
import com.br.foodbridge.controller.dto.mapper.DoacaoMapper.toResponse
import com.br.foodbridge.domain.model.Doacao
import com.br.foodbridge.service.DoacaoService
import com.br.foodbridge.service.OrganizacaoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/doacao")
class DoacaoController(
    private val doacaoService: DoacaoService,
    private val organizacaoService: OrganizacaoService
) {
    @PostMapping("/publicar")
    fun publicar(
        @AuthenticationPrincipal tokenData: TokenData,
        @RequestBody @Valid doacaoDto: DoacaoDTO
    ): ResponseEntity<Doacao?> =
        tokenData.organizacaoId?.let { organizacaoId ->
            val organizacao = organizacaoService.findById(organizacaoId)
            val doacao = doacaoService.criarDoacao(doacaoDto, organizacao)
            ResponseEntity.status(HttpStatus.CREATED).body(toResponse(doacao))
        } ?: ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()


    @GetMapping("/{id}")
    fun listarDoacao(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable id: Long
    ): ResponseEntity<DoacaoDTO> {
        tokenData.organizacaoId?.let {
            return ResponseEntity.ok(doacaoService.listarDoacao(id))
        } ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }

    @GetMapping
    fun listar(
        @AuthenticationPrincipal tokenData: TokenData,
    ): ResponseEntity<List<Doacao>> =
        tokenData.organizacaoId?.let { organizacaoId ->
            val organizacao = organizacaoService.findById(organizacaoId)
            ResponseEntity.ok(doacaoService.listarDoacoesOrganizacao(organizacao))
        } ?: ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

    @PutMapping("/{id}")
    fun editar(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable id: Long,
        @RequestBody @Valid doacao: DoacaoDTO,
    ): ResponseEntity<Doacao?> =
        tokenData.organizacaoId?.let {
            organizacaoId ->
            val organizacao = organizacaoService.findById(organizacaoId)
            val doacao = doacaoService.editarDoacao(doacao, organizacao)
            ResponseEntity.status(HttpStatus.CREATED).body(toResponse(doacao))
        }?: ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

    @DeleteMapping("/{id}")
    fun apagar(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        tokenData.organizacaoId?.let {
            doacaoService.deletarDoacao(id)
            return ResponseEntity.noContent().build()
        }?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }
}
