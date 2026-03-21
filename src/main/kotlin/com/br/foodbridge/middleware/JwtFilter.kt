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

        // 🔓 Rotas públicas que não precisam de JWT
        return path.startsWith("/auth")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val token = extractToken(request)

        // 🔹 Sem token → segue fluxo normal (rotas públicas)
        if (token == null) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val tokenType = jwtService.extractTokenType(token)

            when (tokenType) {

                // 🔐 TOKEN ACCESS → autentica no Spring
                JwtService.TOKEN_TYPE_ACCESS -> {
                    val data = jwtService.extractAccessTokenData(token)

                    RequestContext.set(
                        usuarioId = data.usuarioId,
                        organizacaoId = data.organizacaoId,
                        role = data.role
                    )

                    val authorities = listOf(
                        SimpleGrantedAuthority("ROLE_${data.role}")
                    )

                    val authentication = UsernamePasswordAuthenticationToken(
                        data.usuarioId.toString(),
                        null,
                        authorities
                    )

                    SecurityContextHolder.getContext().authentication = authentication
                }

                // 🟡 TOKEN TEMP → apenas contexto (SEM Spring Security)
                JwtService.TOKEN_TYPE_TEMP -> {
                    val data = jwtService.extractTempTokenData(token)

                    RequestContext.set(
                        usuarioId = data.usuarioId,
                        organizacaoId = null,
                        role = null
                    )

                    // 🚫 NÃO autentica no Spring
                }

                else -> {
                    // tipo desconhecido → ignora
                }
            }

        } catch (ex: Exception) {
            // 🔥 NÃO quebra a request
            // Apenas ignora token inválido

            RequestContext.clear()
            SecurityContextHolder.clearContext()
        }

        try {
            filterChain.doFilter(request, response)
        } finally {
            // 🔹 Sempre limpa contexto custom
            RequestContext.clear()
        }
    }

    // =========================================================
    // 🔹 Helpers
    // =========================================================

    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null

        if (!header.startsWith("Bearer ")) return null

        return header.removePrefix("Bearer ").trim()
    }
}