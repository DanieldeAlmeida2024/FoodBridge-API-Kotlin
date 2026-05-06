package com.br.foodbridge.controller

import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.controller.dto.doacao.DoacaoDTO
import com.br.foodbridge.controller.dto.mapper.DoacaoMapper.toResponse
import com.br.foodbridge.domain.model.Doacao
import com.br.foodbridge.exception.custom.BusinessException
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
@RequestMapping("/doacoes")
class DoacaoController(
    private val doacaoService: DoacaoService
) {

    @PostMapping
    fun criar(
        @AuthenticationPrincipal tokenData: TokenData,
        @RequestBody @Valid request: DoacaoDTO
    ): ResponseEntity<DoacaoDTO> {

        val organizacaoId = tokenData.organizacaoId
            ?: throw BusinessException("Usuário não vinculado a uma organização")

        val doacao = doacaoService.criarDoacao(request, organizacaoId)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(toResponse(doacao))
    }

    @GetMapping("/{id}")
    fun buscarPorId(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable id: Long
    ): ResponseEntity<DoacaoDTO> {

        validarOrganizacao(tokenData)

        return ResponseEntity.ok(
            doacaoService.listarDoacao(id)
        )
    }

    @GetMapping("/disponiveis")
    fun listarDisponiveis(
        @AuthenticationPrincipal tokenData: TokenData
    ): ResponseEntity<List<DoacaoDTO>> {

        validarOrganizacao(tokenData)

        val lista = doacaoService
            .listarDoacoesDisponiveis(tokenData.organizacaoId)
            .map { toResponse(it) }

        return ResponseEntity.ok(lista)
    }

    @GetMapping
    fun listar(
        @AuthenticationPrincipal tokenData: TokenData
    ): ResponseEntity<List<DoacaoDTO>> {

        val organizacaoId = tokenData.organizacaoId
            ?: throw BusinessException("Usuário não vinculado a uma organização")

        val lista = doacaoService
            .listarDoacoesOrganizacao(organizacaoId)
            .map { toResponse(it) }

        return ResponseEntity.ok(lista)
    }

    @PutMapping("/{id}")
    fun atualizar(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable id: Long,
        @RequestBody @Valid request: DoacaoDTO
    ): ResponseEntity<DoacaoDTO> {

        val organizacaoId = tokenData.organizacaoId
            ?: throw BusinessException("Usuário não vinculado a uma organização")

        val atualizado = doacaoService.editarDoacao(
            id,
            request,
            organizacaoId
        )

        return ResponseEntity.ok(toResponse(atualizado))
    }

    @DeleteMapping("/{id}")
    fun deletar(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable id: Long
    ): ResponseEntity<Void> {

        val organizacaoId = tokenData.organizacaoId
            ?: throw BusinessException("Usuário não vinculado a uma organização")

        doacaoService.deletarDoacao(id, organizacaoId)

        return ResponseEntity.noContent().build()
    }


    private fun validarOrganizacao(tokenData: TokenData) {
        if (tokenData.organizacaoId == null) {
            throw BusinessException("Usuário não vinculado a uma organização")
        }
    }

    // Mapper
    private fun toResponse(doacao: Doacao): DoacaoDTO {
        return DoacaoDTO(
            tipoComida = doacao.tipoComida,
            descricaoComida = doacao.descricaoComida,
            quantidade = doacao.quantidade,
            unidade = doacao.unidade,
            dataExpiracao = doacao.dataExpiracao,
            janelasDisponiveis = doacao.janelasDisponiveis,
            status = doacao.status,
            endereco = doacao.endereco,
            organizacao = doacao.organizacao
        )
    }
}
