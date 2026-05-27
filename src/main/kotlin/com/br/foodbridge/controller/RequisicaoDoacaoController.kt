package com.br.foodbridge.controller

import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.controller.dto.requisicao.AprovarRequisicaoDoacaoRequest
import com.br.foodbridge.controller.dto.requisicao.CreateRequisicaoDoacaoRequest
import com.br.foodbridge.controller.dto.requisicao.RequisicaoDoacaoDTO
import com.br.foodbridge.domain.model.RequisicaoDoacao
import com.br.foodbridge.exception.custom.BusinessException
import com.br.foodbridge.service.RequisicaoDoacaoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/requisicoes")
class RequisicaoDoacaoController(
    private val requisicaoDoacaoService: RequisicaoDoacaoService
) {

    @PostMapping
    fun criar(
        @AuthenticationPrincipal tokenData: TokenData,
        @RequestBody @Valid request: CreateRequisicaoDoacaoRequest
    ): ResponseEntity<RequisicaoDoacaoDTO> {
        val requisicao = requisicaoDoacaoService.criar(
            request,
            tokenData.organizacaoId,
            tokenData.role
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(toResponse(requisicao))
    }

    @GetMapping("/minhas")
    fun listarMinhas(
        @AuthenticationPrincipal tokenData: TokenData
    ): ResponseEntity<List<RequisicaoDoacaoDTO>> {
        val lista = requisicaoDoacaoService
            .listarMinhasRequisicoes(tokenData.organizacaoId, tokenData.role)
            .map(::toResponse)

        return ResponseEntity.ok(lista)
    }

    @GetMapping("/recebidas")
    fun listarRecebidas(
        @AuthenticationPrincipal tokenData: TokenData
    ): ResponseEntity<List<RequisicaoDoacaoDTO>> {
        val lista = requisicaoDoacaoService
            .listarRecebidas(tokenData.organizacaoId, tokenData.role)
            .map(::toResponse)

        return ResponseEntity.ok(lista)
    }

    @GetMapping("/{id}")
    fun buscarPorId(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable id: Long
    ): ResponseEntity<RequisicaoDoacaoDTO> {
        val requisicao = requisicaoDoacaoService.buscarPorId(id, tokenData.organizacaoId)
        return ResponseEntity.ok(toResponse(requisicao))
    }

    @PatchMapping("/{id}/vincular-voluntario")
    fun vincularVoluntario(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable id: Long,
        @RequestBody @Valid request: AprovarRequisicaoDoacaoRequest
    ): ResponseEntity<RequisicaoDoacaoDTO> {
        val requisicao = requisicaoDoacaoService.vincularVoluntario(
            id,
            request.voluntarioId,
            tokenData.organizacaoId,
            tokenData.role
        )
        return ResponseEntity.ok(toResponse(requisicao))
    }

    @PatchMapping("/{id}/aprovar")
    fun aprovar(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable id: Long
    ): ResponseEntity<RequisicaoDoacaoDTO> {
        val requisicao = requisicaoDoacaoService.aprovar(id, tokenData.organizacaoId, tokenData.role)
        return ResponseEntity.ok(toResponse(requisicao))
    }

    @PatchMapping("/{id}/rejeitar")
    fun rejeitar(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable id: Long
    ): ResponseEntity<RequisicaoDoacaoDTO> {
        val requisicao = requisicaoDoacaoService.rejeitar(id, tokenData.organizacaoId, tokenData.role)
        return ResponseEntity.ok(toResponse(requisicao))
    }

    @PatchMapping("/{id}/cancelar")
    fun cancelar(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable id: Long
    ): ResponseEntity<RequisicaoDoacaoDTO> {
        val requisicao = requisicaoDoacaoService.cancelar(id, tokenData.organizacaoId, tokenData.role)
        return ResponseEntity.ok(toResponse(requisicao))
    }

    @PatchMapping("/{id}/concluir")
    fun concluir(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable id: Long
    ): ResponseEntity<RequisicaoDoacaoDTO> {
        val requisicao = requisicaoDoacaoService.concluir(id, tokenData.organizacaoId, tokenData.role)
        return ResponseEntity.ok(toResponse(requisicao))
    }

    private fun toResponse(requisicao: RequisicaoDoacao): RequisicaoDoacaoDTO {
        val id = requisicao.id ?: throw BusinessException("Requisição sem ID")
        val doacaoId = requisicao.doacao.id ?: throw BusinessException("Doação sem ID")
        val organizacaoSolicitanteId = requisicao.organizacaoSolicitante.id
            ?: throw BusinessException("Organização solicitante sem ID")
        val organizacaoDoadoraId = requisicao.doacao.organizacao.id
            ?: throw BusinessException("Organização doadora sem ID")

        return RequisicaoDoacaoDTO(
            id = id,
            doacaoId = doacaoId,
            organizacaoSolicitanteId = organizacaoSolicitanteId,
            organizacaoSolicitanteNome = requisicao.organizacaoSolicitante.nome,
            organizacaoDoadoraId = organizacaoDoadoraId,
            organizacaoDoadoraNome = requisicao.doacao.organizacao.nome,
            voluntarioId = requisicao.voluntario?.id,
            voluntarioNome = requisicao.voluntario?.nome,
            quantidadeSolicitada = requisicao.quantidadeSolicitada,
            observacao = requisicao.observacao,
            status = requisicao.status,
            statusDoacao = requisicao.doacao.status,
            createdAt = requisicao.createdAt,
            updatedAt = requisicao.updatedAt,
            respondedAt = requisicao.respondedAt,
            completedAt = requisicao.completedAt
        )
    }
}
