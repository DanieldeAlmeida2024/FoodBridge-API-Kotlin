package com.br.foodbridge.controller

import com.br.foodbridge.controller.dto.organizacao.OrganizacaoDTO
import com.br.foodbridge.controller.dto.usuario.UsuarioDTO
import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.exception.custom.ValidationException
import org.springframework.security.core.annotation.AuthenticationPrincipal
import com.br.foodbridge.service.AdminService
import com.br.foodbridge.service.UsuarioService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
class AdminController(
    private val adminService: AdminService,
) {

    @GetMapping("/organizacoes")
    fun listarOrganizacoes(): ResponseEntity<List<OrganizacaoDTO>> {
        return ResponseEntity.ok(adminService.listarOrganizacoes())
    }

    @GetMapping("/organizacoes/pendentes")
    fun listarOrganizacoesPendentes(): ResponseEntity<List<OrganizacaoDTO>> {
        return ResponseEntity.ok(adminService.listarOrganizacoesPendentes())
    }

    @PatchMapping("/organizacoes/{id}/aprovar")
    fun aprovarOrganizacao(
        @PathVariable id: Long
    ): ResponseEntity<Void> {

        adminService.aprovarOrganizacao(id)

        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/organizacoes/{id}/reprovar")
    fun reprovarOrganizacao(
        @PathVariable id: Long
    ): ResponseEntity<Void> {

        adminService.reprovarOrganizacao(id)

        return ResponseEntity.noContent().build()
    }

    @GetMapping("/usuarios")
    fun listarUsuarios(): ResponseEntity<List<UsuarioDTO>> {
        return ResponseEntity.ok(adminService.listarUsuarios())
    }

    @GetMapping("/usuarios/pendentes")
    fun listarUsuariosPendentes(): ResponseEntity<List<UsuarioDTO>> {
        return ResponseEntity.ok(adminService.listarUsuariosPendentes())
    }

    @PatchMapping("/usuarios/{id}/aprovar")
    fun aprovarUsuario(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable id: Long
    ): ResponseEntity<Void> {

        val adminId = tokenData.usuarioId
            ?: throw ValidationException("Usuário inválido no token")

        adminService.aprovarUsuario(id, adminId)

        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/usuarios/{id}/reprovar")
    fun reprovarUsuario(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable id: Long
    ): ResponseEntity<Void> {

        val adminId = tokenData.usuarioId
            ?: throw ValidationException("Usuário inválido no token")

        adminService.reprovarUsuario(id, adminId)

        return ResponseEntity.noContent().build()
    }
}