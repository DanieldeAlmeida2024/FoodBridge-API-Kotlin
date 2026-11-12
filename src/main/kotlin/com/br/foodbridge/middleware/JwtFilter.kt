package com.br.foodbridge.middleware

import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.exception.custom.BusinessException
import com.br.foodbridge.service.utils.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.util.AntPathMatcher
import org.springframework.web.servlet.HandlerExceptionResolver

@Component
@Profile("!schema-validation")
class JwtFilter(
    private val jwtService: JwtService,
    private val handlerExceptionResolver: HandlerExceptionResolver
) : OncePerRequestFilter() {

    private val pathMatcher = AntPathMatcher()

    private val tempOnlyRoutes = listOf(
        RouteRule("POST", "/organizacoes"),
        RouteRule("GET", "/organizacoes/cnpj/**"),
        RouteRule("ANY", "/auth/select-org")
    )

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val caminho = request.requestURI.removePrefix(request.contextPath)
        return caminho == "/auth/login" ||
            caminho == "/error" ||
            (request.method == "POST" && caminho == "/usuarios") ||
            (request.method == "GET" && caminho == "/dashboards/publico")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val token = extractToken(request)

        if (token.isNullOrBlank()) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            val tokenType = jwtService.extractTokenType(token)

            val isTempRoute = isTempRoute(request.method, request.requestURI.removePrefix(request.contextPath))

            val tokenData = extractTokenData(token, tokenType)

            validateTokenRoute(tokenType, isTempRoute)

            validateUserStatus(tokenType, tokenData)

            val authorities = buildAuthorities(tokenType, tokenData)

            SecurityContextHolder.getContext().authentication =
                UsernamePasswordAuthenticationToken(tokenData, null, authorities)

            filterChain.doFilter(request, response)

        } catch (ex: Exception) {
            SecurityContextHolder.clearContext()
            handlerExceptionResolver.resolveException(request, response, null, ex)
        }
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization")
        if (header == null || !header.startsWith("Bearer ")) return null
        return header.removePrefix("Bearer ").trim()
    }

    private fun isTempRoute(method: String, path: String): Boolean {
        val result = tempOnlyRoutes.any { rule -> (rule.method == "ANY" || rule.method == method) && pathMatcher.match(rule.pattern, path) }
        return result
    }

    private fun extractTokenData(token: String, tokenType: String): TokenData {
        return when (tokenType) {
            JwtService.TOKEN_TYPE_ACCESS -> jwtService.extractAccessTokenData(token)
            JwtService.TOKEN_TYPE_TEMP -> jwtService.extractTempTokenData(token)
            else -> throw BusinessException("Token inválido ou mal formado")
        } ?: throw BusinessException("Token inválido ou mal formado")
    }

    private fun validateTokenRoute(tokenType: String, isTempRoute: Boolean) {
        if (isTempRoute && tokenType != JwtService.TOKEN_TYPE_TEMP)
            throw BusinessException("Apenas token temporário permitido")
        if (!isTempRoute && tokenType == JwtService.TOKEN_TYPE_TEMP)
            throw BusinessException("Token temporário não permitido nesta rota")
    }

    private fun validateUserStatus(tokenType: String, data: TokenData) {
        if (tokenType == JwtService.TOKEN_TYPE_ACCESS && data.role != "ADMIN" && data.status != "VERIFICADO")
            throw BusinessException("Usuário não verificado")
    }

    private fun buildAuthorities(tokenType: String, data: TokenData): List<GrantedAuthority> = listOf(
        SimpleGrantedAuthority("TOKEN_$tokenType"),
        SimpleGrantedAuthority("ROLE_${data.role}"),
        SimpleGrantedAuthority("STATUS_${data.status}")
    )

    data class RouteRule(val method: String, val pattern: String)
}
