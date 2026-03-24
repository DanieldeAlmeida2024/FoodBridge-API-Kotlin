package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.auth.LoginRequest
import com.br.foodbridge.controller.dto.auth.LoginResponse
import com.br.foodbridge.controller.dto.organizacao.OrganizacaoResumoDTO
import com.br.foodbridge.domain.enums.OrganizacaoRole
import com.br.foodbridge.domain.enums.StatusOrganizacao
import com.br.foodbridge.domain.model.UsuarioOrganizacao
import com.br.foodbridge.domain.repository.UsuarioOrganizacaoRepository
import com.br.foodbridge.domain.repository.UsuarioRepository
import com.br.foodbridge.service.utils.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService (
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val usuarioRepository: UsuarioRepository,
    private val usuarioOrganizacaoRepository: UsuarioOrganizacaoRepository
){

    fun login(request: LoginRequest): LoginResponse {
        // Busca o usuário pelo email
        val usuario = usuarioRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("Usuário não encontrado")

        // Valida senha
        if (!passwordEncoder.matches(request.senha, usuario.senha)) {
            throw IllegalArgumentException("Senha inválida")
        }

        // Busca todos vínculos do usuário
        val vinculacoes = usuarioOrganizacaoRepository.findAllByUsuarioId(usuario.id!!)

        // Monta lista de organizações vinculadas
        val organizacoes = vinculacoes.map {
            OrganizacaoResumoDTO(
                organizacaoId = it.organizacao?.id!!,
                nome = it.organizacao?.nome,
                role = it.role,
                status = it.status
            )
        }

        // Verifica se é admin de alguma organização verificada
        val adminOrg = vinculacoes.firstOrNull {
            it.role == OrganizacaoRole.ADMIN && it.status == StatusOrganizacao.VERIFICADO
        }

        // Gera token final ou temporário
        val token = if (adminOrg != null) {
            // busca o ID do vinculo entre a organização e o usuario
            val vinculo = getVinculoOrThrowForAdminLogin(usuario.id, adminOrg.organizacao?.id)
            val vinculoId = vinculo.id?: throw IllegalArgumentException("vinculo não encontrado")

            jwtService.generateAccessToken(
                usuarioId = usuario.id,
                organizacaoId = adminOrg.organizacao?.id!!,
                vinculoId = vinculoId,
                role = adminOrg.role.name
            )
        } else {
            jwtService.generateTempToken(usuario.id)
        }

        // Retorna resposta
        return LoginResponse(
            tempToken = token,
            nome = usuario.nome,
            email = usuario.email,
            organizacoes = organizacoes
        )
    }

    /**
     * Seleciona uma organização usando token TEMP e gera token final.
     */
    fun selectOrganization(usuarioId: Long, organizacaoId: Long): LoginResponse {
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { IllegalArgumentException("Usuário não encontrado") }

        val vinculo = usuarioOrganizacaoRepository
            .findByUsuarioIdAndOrganizacaoId(usuarioId, organizacaoId)
            ?: throw IllegalArgumentException("Usuário não vinculado a essa organização")

        if (vinculo.status != StatusOrganizacao.VERIFICADO) {
            throw IllegalArgumentException("Usuário ainda não aprovado nessa organização")
        }

        val token = jwtService.generateAccessToken(
            usuarioId = usuario.id!!,
            organizacaoId = organizacaoId,
            vinculoId = vinculo.id!!,
            role = vinculo.role.name
        )

        val organizacoes = usuarioOrganizacaoRepository.findAllByUsuarioId(usuario.id)
            .map {
                OrganizacaoResumoDTO(
                    organizacaoId = it.organizacao?.id!!,
                    nome = it.organizacao?.nome,
                    role = it.role,
                    status = it.status
                )
            }

        return LoginResponse(
            tempToken = token,
            nome = usuario.nome,
            email = usuario.email,
            organizacoes = organizacoes
        )
    }
    // Helpers

    private fun getVinculoOrThrowForAdminLogin(
        userId: Long,
        organizacaoId: Long?
    ): UsuarioOrganizacao =
        usuarioOrganizacaoRepository
            .findByUsuarioIdAndOrganizacaoId(userId, organizacaoId)
            ?: throw IllegalArgumentException("Vínculo não encontrado")

}