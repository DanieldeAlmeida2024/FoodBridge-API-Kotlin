package com.br.foodbridge.controller


import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.controller.dto.voluntario.CreateUpdateVoluntario
import com.br.foodbridge.controller.dto.voluntario.VoluntarioDTO
import com.br.foodbridge.domain.model.Voluntario
import com.br.foodbridge.exception.custom.BusinessException
import com.br.foodbridge.service.VoluntarioService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/voluntarios")
class VoluntarioController(
    private val voluntarioService: VoluntarioService
) {

    @PostMapping
    fun criarOuVincular(
        @AuthenticationPrincipal tokenData: TokenData,
        @Valid @RequestBody request: CreateUpdateVoluntario
    ): ResponseEntity<VoluntarioDTO> {

        val voluntario = voluntarioService.criarOuVincular(request, tokenData)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(toResponse(voluntario))
    }

    @GetMapping("/{id}")
    fun buscarPorId(
        @PathVariable id: Long
    ): ResponseEntity<VoluntarioDTO> {

        val voluntario = voluntarioService.findById(id)

        return ResponseEntity.ok(toResponse(voluntario))
    }

    @GetMapping("/cpf/{cpf}")
    fun buscarPorCpf(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable cpf: String
    ): ResponseEntity<VoluntarioDTO> {

        val voluntario = voluntarioService.findByCpf(cpf)

        return ResponseEntity.ok(toResponse(voluntario))
    }

    @GetMapping
    fun listar(): ResponseEntity<List<VoluntarioDTO>> {

        val lista = voluntarioService.findAll()
            .map { toResponse(it) }

        return ResponseEntity.ok(lista)
    }

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @Valid @RequestBody request: CreateUpdateVoluntario
    ): ResponseEntity<VoluntarioDTO> {

        val atualizado = voluntarioService.update(id, request)

        return ResponseEntity.ok(toResponse(atualizado))
    }

    @PatchMapping("/{id}/desativar")
    fun desativar(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable id: Long
    ): ResponseEntity<Void> {

        val organizacaoId = tokenData.organizacaoId
            ?: throw BusinessException("Usuário não vinculado a uma organização")

        voluntarioService.desativarVinculo(id, organizacaoId)

        return ResponseEntity.noContent().build()
    }

    // Mapper
    private fun toResponse(voluntario: Voluntario): VoluntarioDTO {

        val id = voluntario.id
            ?: throw BusinessException("Voluntário sem ID")

        return VoluntarioDTO(
            id = id,
            nome = voluntario.nome,
            cpf = voluntario.cpf,
            email = voluntario.email,
            telefone = voluntario.telefone,
            endereco = voluntario.endereco
        )
    }
}
