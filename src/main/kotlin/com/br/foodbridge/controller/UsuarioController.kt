package com.br.foodbridge.controller

import com.br.foodbridge.controller.dto.usuario.*
import com.br.foodbridge.controller.dto.organizacao.OrganizacaoResumoDTO
import com.br.foodbridge.domain.model.Usuario

import com.br.foodbridge.controller.dto.auth.TokenData
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

    @PostMapping("/criar")
    fun criarUsuario(
        @RequestBody request: CreateUpdateUserRequest
    ): ResponseEntity<UsuarioDTO?> {

        // Usuário pode ser null na criação
        val usuario = usuarioService.createUser(request)

        // Retorna DTO sem senha
        return ResponseEntity.status(HttpStatus.CREATED).body(
            UsuarioDTO(
                id = usuario.id,
                nome = usuario.nome,
                email = usuario.email,
                status = usuario.status,
            )
        )
    }


    // Usuário logado
    @GetMapping("/eu")
    fun getMe(
        @AuthenticationPrincipal tokenData: TokenData
    ): ResponseEntity<Usuario> {
        val usuario = usuarioService.findByIdEntity(tokenData.usuarioId)
        return ResponseEntity.ok(usuario)
    }

    // Organizações vinculadas do usuário logado
    @GetMapping("/eu/organizacoes")
    fun getMyOrganizations(
        @AuthenticationPrincipal tokenData: TokenData
    ): ResponseEntity<List<OrganizacaoResumoDTO>> {
        val organizacoes = usuarioService.listarOrganizacoesDoUsuario(tokenData.usuarioId)
        return ResponseEntity.ok(organizacoes)
    }

    // Atualizar dados do usuário logado
    @PutMapping("/eu")
    fun updateMe(
        @AuthenticationPrincipal tokenData: TokenData,
        @RequestBody request: CreateUpdateUserRequest
    ): ResponseEntity<UsuarioResponse> {
        val usuarioAtualizado = usuarioService.update(tokenData.usuarioId, request)
        return ResponseEntity.ok(usuarioAtualizado)
    }

    // Deletar conta do usuário logado
    @DeleteMapping("/eu")
    fun deleteMe(
        @AuthenticationPrincipal tokenData: TokenData
    ): ResponseEntity<Void> {
        usuarioService.delete(tokenData.usuarioId)
        return ResponseEntity.noContent().build()
    }
}