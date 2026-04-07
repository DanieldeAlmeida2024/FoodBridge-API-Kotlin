package com.br.foodbridge.middleware

import com.br.foodbridge.service.utils.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter


@Component
class JwtFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        // Permite pular o login, mas não o select-org
        return path.startsWith("/auth/login")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val token = extractToken(request)

        if (token == null) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val tokenType = jwtService.extractTokenType(token)

            val data = when (tokenType) {
                JwtService.TOKEN_TYPE_ACCESS -> jwtService.extractAccessTokenData(token)
                JwtService.TOKEN_TYPE_TEMP -> jwtService.extractTempTokenData(token)
                else -> null
            }

            data?.let {
                val authorities = listOf(SimpleGrantedAuthority("ROLE_${it.role ?: "USER"}"))

                val authentication = UsernamePasswordAuthenticationToken(
                    it,
                    null,
                    authorities
                )

                SecurityContextHolder.getContext().authentication = authentication
            }

        } catch (ex: Exception) {
            // Limpa o contexto, mas NÃO interrompe a requisição
            SecurityContextHolder.clearContext()
            // Apenas loga, não envia erro
            logger.warn("Token inválido ou expirado: ${ex.message}")
        }

        // Continua normalmente, mesmo sem token válido
        filterChain.doFilter(request, response)
    }

    // Helpers
    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null

        if (!header.startsWith("Bearer ")) return null

        return header.removePrefix("Bearer ").trim()
    }
}