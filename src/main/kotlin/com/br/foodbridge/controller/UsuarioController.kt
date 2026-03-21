package com.br.foodbridge.controller

import com.br.foodbridge.controller.dto.usuario.*
import com.br.foodbridge.controller.dto.organizacao.OrganizacaoResumoDTO
import com.br.foodbridge.domain.model.Usuario

import com.br.foodbridge.middleware.RequestContext
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

        // ✅ NÃO chama RequestContext, usuário pode ser null
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


    // 🔍 Usuário logado
    @GetMapping("/eu")
    fun getMe(): ResponseEntity<Usuario> {
        val usuario =RequestContext.getUsuario(usuarioService)
            ?: throw IllegalStateException("Usuário não autenticado")
        return ResponseEntity.ok(usuario)
    }

    // 🔗 Organizações vinculadas do usuário logado
    @GetMapping("/eu/organizacoes")
    fun getMyOrganizations(): ResponseEntity<List<OrganizacaoResumoDTO>> {
        val organizacoes = usuarioService.listarOrganizacoesDoUsuario()
        return ResponseEntity.ok(organizacoes)
    }

    // ✏️ Atualizar dados do usuário logado
    @PutMapping("/eu")
    fun updateMe(@RequestBody request: CreateUpdateUserRequest): ResponseEntity<UsuarioResponse> {
        val usuarioAtualizado = usuarioService.update(request)
        return ResponseEntity.ok(usuarioAtualizado)
    }

    // ❌ Deletar conta do usuário logado
    @DeleteMapping("/eu")
    fun deleteMe(): ResponseEntity<Void> {
        usuarioService.delete()
        return ResponseEntity.noContent().build()
    }
}