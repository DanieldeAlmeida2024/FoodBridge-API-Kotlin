package com.br.foodbridge.controller

import com.br.foodbridge.controller.dto.usuario.*
import com.br.foodbridge.controller.dto.organizacao.OrganizacaoResumoDTO
import com.br.foodbridge.domain.model.Usuario

import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.exception.custom.BusinessException
import com.br.foodbridge.exception.custom.ValidationException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import com.br.foodbridge.service.UsuarioService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/usuarios")
class UsuarioController(
    private val usuarioService: UsuarioService
) {

    @PostMapping
    fun criarUsuario(
        @RequestBody request: CreateUpdateUserRequest
    ): ResponseEntity<UsuarioResponse> {

        val usuario = usuarioService.createUser(request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(toResponse(usuario))
    }

    @GetMapping("/me")
    fun getMe(
        @AuthenticationPrincipal tokenData: TokenData
    ): ResponseEntity<UsuarioResponse> {

        val usuarioId = tokenData.usuarioId
            ?: throw ValidationException("Usuário inválido no token")

        val usuario = usuarioService.findByIdEntity(usuarioId)

        return ResponseEntity.ok(toResponse(usuario))
    }

    @GetMapping("/me/organizacoes")
    fun getMyOrganizations(
        @AuthenticationPrincipal tokenData: TokenData
    ): ResponseEntity<List<OrganizacaoResumoDTO>> {

        val usuarioId = tokenData.usuarioId
            ?: throw ValidationException("Usuário inválido no token")

        val organizacoes = usuarioService
            .listarOrganizacoesDoUsuario(usuarioId)

        return ResponseEntity.ok(organizacoes)
    }

    @PutMapping("/me")
    fun updateMe(
        @AuthenticationPrincipal tokenData: TokenData,
        @RequestBody request: CreateUpdateUserRequest
    ): ResponseEntity<UsuarioResponse> {

        val usuarioId = tokenData.usuarioId
            ?: throw ValidationException("Usuário inválido no token")

        val atualizado = usuarioService.update(usuarioId, request)

        return ResponseEntity.ok(atualizado)
    }

    @DeleteMapping("/me")
    fun deleteMe(
        @AuthenticationPrincipal tokenData: TokenData
    ): ResponseEntity<Void> {

        val usuarioId = tokenData.usuarioId
            ?: throw ValidationException("Usuário inválido no token")

        usuarioService.delete(usuarioId)

        return ResponseEntity.noContent().build()
    }

    // Mapper
    private fun toResponse(usuario: Usuario): UsuarioResponse {

        val id = usuario.id
            ?: throw BusinessException("Usuário sem ID")

        return UsuarioResponse(
            id = id,
            nome = usuario.nome,
            email = usuario.email,
            status = usuario.status,
        )
    }
}