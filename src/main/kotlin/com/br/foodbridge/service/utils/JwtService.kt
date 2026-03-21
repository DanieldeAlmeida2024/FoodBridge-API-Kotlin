package com.br.foodbridge.service.utils

import com.br.foodbridge.config.JwtConfig
import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.domain.enums.StatusOrganizacao
import com.br.foodbridge.domain.enums.UserStatus
import com.br.foodbridge.domain.repository.OrganizacaoRepository
import com.br.foodbridge.domain.repository.UsuarioOrganizacaoRepository
import com.br.foodbridge.domain.repository.UsuarioRepository
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date

@Component
class JwtService(
    private val jwtConfig: JwtConfig,
    private val usuarioOrganizacaoRepository: UsuarioOrganizacaoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val organizacaoRepository: OrganizacaoRepository
) {

    companion object {
        private const val CLAIM_TYPE = "type"
        private const val CLAIM_ORG_ID = "organizacaoId"
        private const val CLAIM_ROLE = "role"

        const val TOKEN_TYPE_TEMP = "TEMP"
        const val TOKEN_TYPE_ACCESS = "ACCESS"

        private const val TEMP_EXPIRATION = 5 * 60 * 1000L // 5 minutos
    }

    // 🔐 Chave única
    private fun getKey() =
        Keys.hmacShaKeyFor(jwtConfig.secret.toByteArray(StandardCharsets.UTF_8))

    // 🔐 Parser centralizado
    private fun parse(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(getKey())
            .build()
            .parseClaimsJws(token)
            .body

    // =========================================================
    // 🔹 GERAÇÃO DE TOKENS
    // =========================================================

    fun generateTempToken(usuarioId: Long): String =
        buildToken(
            subject = usuarioId.toString(),
            claims = mapOf(CLAIM_TYPE to TOKEN_TYPE_TEMP),
            expiration = TEMP_EXPIRATION
        )

    fun generateAccessToken(
        usuarioId: Long,
        organizacaoId: Long,
        role: String
    ): String =
        buildToken(
            subject = usuarioId.toString(),
            claims = mapOf(
                CLAIM_TYPE to TOKEN_TYPE_ACCESS,
                CLAIM_ORG_ID to organizacaoId,
                CLAIM_ROLE to role
            ),
            expiration = jwtConfig.expiration
        )

    private fun buildToken(
        subject: String,
        claims: Map<String, Any>,
        expiration: Long
    ): String {
        val now = System.currentTimeMillis()

        return Jwts.builder()
            .setSubject(subject)
            .addClaims(claims)
            .setIssuedAt(Date(now))
            .setExpiration(Date(now + expiration))
            .signWith(getKey())
            .compact()
    }

    // =========================================================
    // 🔹 EXTRAÇÃO DE DADOS
    // =========================================================

    fun extractTokenType(token: String): String =
        parse(token)[CLAIM_TYPE]?.toString()
            ?: throw IllegalStateException("Token sem tipo")

    fun extractTempTokenData(token: String): TokenData {
        val claims = parse(token)

        require(extractTokenType(token) == TOKEN_TYPE_TEMP) {
            "Token inválido (não é TEMP)"
        }

        return TokenData(
            token = token,
            usuarioId = claims.subject.toLong(),
            organizacaoId = null,
            role = null
        )
    }

    fun extractAccessTokenData(token: String): TokenData {
        val claims = parse(token)

        require(extractTokenType(token) == TOKEN_TYPE_ACCESS) {
            "Token inválido (não é ACCESS)"
        }

        val usuarioId = claims.subject.toLong()
        val organizacaoId = claims[CLAIM_ORG_ID]?.toString()?.toLong()
            ?: throw IllegalStateException("Token sem organizacaoId")

        val role = claims[CLAIM_ROLE]?.toString()
            ?: throw IllegalStateException("Token sem role")

        return TokenData(
            token = token,
            usuarioId = usuarioId,
            organizacaoId = organizacaoId,
            role = role
        )
    }

    // =========================================================
    // 🔹 CONVERSÃO TEMP → ACCESS
    // =========================================================

    fun generateAccessFromTemp(tempToken: String, organizacaoId: Long): TokenData {
        // Extrai usuário do token TEMP
        val usuarioId = extractTempTokenData(tempToken).usuarioId

        // Busca usuário e organização, lançando erro caso não existam
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { IllegalArgumentException("Usuário não encontrado") }

        val organizacao = organizacaoRepository.findById(organizacaoId)
            .orElseThrow { IllegalArgumentException("Organização não encontrada") }

        // Busca vínculo entre usuário e organização
        val vinculo = usuarioOrganizacaoRepository
            .findByUsuarioIdAndOrganizacaoId(usuarioId, organizacaoId)
            ?: throw IllegalArgumentException("Vínculo não encontrado")

        // Valida os status necessários
        if (vinculo.status != StatusOrganizacao.VERIFICADO) {
            throw IllegalStateException("Usuário não aprovado na organização")
        }

        if (usuario.status != UserStatus.VERIFICADO) {
            throw IllegalStateException("Usuário ainda não foi aprovado")
        }

        if (organizacao.status != StatusOrganizacao.VERIFICADO) {
            throw IllegalStateException("Organização ainda não foi aprovada")
        }

        // Gera token de acesso final (ACCESS)
        val token = generateAccessToken(
            usuarioId = usuarioId,
            organizacaoId = organizacaoId,
            role = vinculo.role.name
        )

        return TokenData(
            token = token,
            usuarioId = usuarioId,
            organizacaoId = organizacaoId,
            role = vinculo.role.name
        )
    }
}