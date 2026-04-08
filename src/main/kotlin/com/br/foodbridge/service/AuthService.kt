package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.auth.LoginRequest
import com.br.foodbridge.controller.dto.auth.LoginResponse
import com.br.foodbridge.controller.dto.organizacao.OrganizacaoResumoDTO
import com.br.foodbridge.domain.enums.OrganizacaoRole
import com.br.foodbridge.domain.enums.StatusOrganizacao
import com.br.foodbridge.domain.model.UsuarioOrganizacao
import com.br.foodbridge.domain.repository.UsuarioOrganizacaoRepository
import com.br.foodbridge.domain.repository.UsuarioRepository
import com.br.foodbridge.exception.custom.BusinessException
import com.br.foodbridge.exception.custom.ResourceNotFoundException
import com.br.foodbridge.exception.custom.ValidationException
import com.br.foodbridge.service.utils.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val usuarioRepository: UsuarioRepository,
    private val usuarioOrganizacaoRepository: UsuarioOrganizacaoRepository
) {

    fun login(request: LoginRequest): LoginResponse {

        if (request.email.isBlank()) {
            throw ValidationException("Email é obrigatório")
        }

        if (request.senha.isBlank()) {
            throw ValidationException("Senha é obrigatória")
        }

        val usuario = usuarioRepository.findByEmail(request.email)
            ?: throw ResourceNotFoundException("Usuário não encontrado")

        if (!passwordEncoder.matches(request.senha, usuario.senha)) {
            throw BusinessException("Credenciais inválidas")
        }

        val usuarioId = usuario.id
            ?: throw BusinessException("Usuário inválido")

        val vinculacoes = usuarioOrganizacaoRepository.findAllByUsuarioId(usuarioId)

        val organizacoes = mapearOrganizacoes(vinculacoes)

        val adminOrg = vinculacoes.firstOrNull {
            it.role == OrganizacaoRole.ADMIN &&
                    it.status == StatusOrganizacao.VERIFICADO
        }

        val token = if (adminOrg != null) {

            val organizacaoId = adminOrg.organizacao?.id
                ?: throw BusinessException("Organização inválida")

            val vinculo = getVinculoOrThrow(usuarioId, organizacaoId)

            val vinculoId = vinculo.id
                ?: throw BusinessException("Vínculo inválido")

            jwtService.generateAccessToken(
                usuarioId = usuarioId,
                organizacaoId = organizacaoId,
                vinculoId = vinculoId,
                role = adminOrg.role.name
            )

        } else {
            jwtService.generateTempToken(usuarioId)
        }

        return LoginResponse(
            tempToken = token,
            nome = usuario.nome,
            email = usuario.email,
            organizacoes = organizacoes,
            status = usuario.status
        )
    }

    fun selectOrganization(usuarioId: Long?, organizacaoId: Long?): LoginResponse {

        if (usuarioId == null || usuarioId <= 0) {
            throw ValidationException("ID do usuário inválido")
        }

        if (organizacaoId == null || organizacaoId <= 0) {
            throw ValidationException("ID da organização inválido")
        }

        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { ResourceNotFoundException("Usuário não encontrado") }

        val vinculo = usuarioOrganizacaoRepository
            .findByUsuarioIdAndOrganizacaoId(usuarioId, organizacaoId)
            ?: throw ResourceNotFoundException("Usuário não vinculado a essa organização")

        if (vinculo.status != StatusOrganizacao.VERIFICADO) {
            throw BusinessException("Usuário ainda não aprovado nessa organização")
        }

        val vinculoId = vinculo.id
            ?: throw BusinessException("Vínculo inválido")

        val token = jwtService.generateAccessToken(
            usuarioId = usuario.id!!,
            organizacaoId = organizacaoId,
            vinculoId = vinculoId,
            role = vinculo.role.name
        )

        val organizacoes = mapearOrganizacoes(
            usuarioOrganizacaoRepository.findAllByUsuarioId(usuarioId)
        )

        return LoginResponse(
            tempToken = token,
            nome = usuario.nome,
            email = usuario.email,
            organizacoes = organizacoes,
            status = usuario.status
        )
    }

    // HELPERS
    private fun getVinculoOrThrow(
        usuarioId: Long,
        organizacaoId: Long
    ): UsuarioOrganizacao =
        usuarioOrganizacaoRepository
            .findByUsuarioIdAndOrganizacaoId(usuarioId, organizacaoId)
            ?: throw ResourceNotFoundException("Vínculo não encontrado")

    private fun mapearOrganizacoes(
        vinculacoes: List<UsuarioOrganizacao>
    ): List<OrganizacaoResumoDTO> {

        return vinculacoes.map {

            val organizacao = it.organizacao
                ?: throw BusinessException("Vínculo com organização inválido")

            val organizacaoId = organizacao.id
                ?: throw BusinessException("Organização sem ID")

            OrganizacaoResumoDTO(
                organizacaoId = organizacaoId,
                nome = organizacao.nome,
                role = it.role,
                status = it.status
            )
        }
    }
}