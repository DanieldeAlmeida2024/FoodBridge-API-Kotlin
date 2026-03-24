package com.br.foodbridge.controller

import com.br.foodbridge.controller.dto.organizacao.OrganizacaoDTO
import com.br.foodbridge.controller.dto.usuario.UsuarioDTO
import com.br.foodbridge.controller.dto.auth.TokenData
import org.springframework.security.core.annotation.AuthenticationPrincipal
import com.br.foodbridge.service.AdminService
import com.br.foodbridge.service.UsuarioService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin")
class AdminController(
    private val adminService: AdminService,
    private val usuarioService: UsuarioService
) {

    // Listar todas organizações
    @GetMapping("/organizacoes")
    fun listarOrganizacoes(): ResponseEntity<List<OrganizacaoDTO>?> {
        val organizacoes = adminService.listarOrganizacoes()
        return ResponseEntity.ok(organizacoes)
    }

    // Listar organizações pendentes de validação
    @GetMapping("/organizacoes/pendentes")
    fun listarOrganizacoesPendentes(): ResponseEntity<List<OrganizacaoDTO>> {
        val pendentes = adminService.listarOrganizacoesPendentes()
        return ResponseEntity.ok(pendentes)
    }

    // Listar todos os usuários
    @GetMapping("/usuarios")
    fun listarUsuarios(): ResponseEntity<List<UsuarioDTO>?> {
        val usuarios = adminService.listarUsuarios()
        return ResponseEntity.ok(usuarios)
    }

    // Listar usuários pendentes de ativação
    @GetMapping("/usuarios/pendentes")
    fun listarUsuariosPendentes(): ResponseEntity<List<UsuarioDTO>> {
        val pendentes = adminService.listarUsuariosPendentes()
        return ResponseEntity.ok(pendentes)
    }

    // Aprovar organização
    @PostMapping("/organizacoes/{id}/aprovar")
    fun aprovarOrganizacao(@PathVariable id: Long): ResponseEntity<Void> {
        adminService.aprovarOrganizacao(id)
        return ResponseEntity.noContent().build()
    }

    // Reprovar organização
    @PostMapping("/organizacoes/{id}/reprovar")
    fun reprovarOrganizacao(@PathVariable id: Long): ResponseEntity<Void> {
        adminService.reprovarOrganizacao(id)
        return ResponseEntity.noContent().build()
    }

    // Aprovar usuário
    @PostMapping("/usuarios/{id}/aprovar")
    fun aprovarUsuario(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        adminService.aprovarUsuario(id, tokenData.usuarioId)
        return ResponseEntity.noContent().build()
    }

    // Reprovar usuário
    @PostMapping("/usuarios/{id}/reprovar")
    fun reprovarUsuario(
        @AuthenticationPrincipal tokenData: TokenData,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        adminService.reprovarUsuario(id, tokenData.usuarioId)
        return ResponseEntity.noContent().build()
    }
}