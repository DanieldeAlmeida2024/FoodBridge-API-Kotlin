package com.br.foodbridge.middleware

import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.domain.enums.OrganizacaoRole
import com.br.foodbridge.domain.enums.UserStatus
import com.br.foodbridge.exception.custom.BusinessException
import com.br.foodbridge.service.utils.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.util.AntPathMatcher
import org.springframework.web.servlet.HandlerExceptionResolver

@Component
class JwtFilter(
    private val jwtService: JwtService,
    private val handlerExceptionResolver: HandlerExceptionResolver
) : OncePerRequestFilter() {

    private val pathMatcher = AntPathMatcher()

    private val publicPaths = listOf("/auth/login", "/usuarios/criar")
    private val tempOnlyRoutes = listOf(
        RouteRule("POST", "/organizacoes"),
        RouteRule("GET", "/organizacoes/cnpj/**"),
        RouteRule("ANY", "/auth/select-org")
    )

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        println("[JwtFilter] shouldNotFilter - URI: ${request.requestURI}")
        val result = publicPaths.any { pattern -> pathMatcher.match("$pattern**", request.requestURI) }
        println("[JwtFilter] shouldNotFilter result: $result")
        return result
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        println("[JwtFilter] doFilterInternal - Start for URI: ${request.requestURI}, Method: ${request.method}")

        val token = extractToken(request)
        println("[JwtFilter] Extracted token: $token")

        if (token.isNullOrBlank()) {
            println("[JwtFilter] No token found, continuing filter chain")
            filterChain.doFilter(request, response)
            return
        }

        try {
            val tokenType = jwtService.extractTokenType(token)
            println("[JwtFilter] Token type: $tokenType")

            val isTempRoute = isTempRoute(request.method, request.requestURI)
            println("[JwtFilter] isTempRoute: $isTempRoute")

            val tokenData = extractTokenData(token, tokenType)
            println("[JwtFilter] TokenData extracted: $tokenData")

            validateTokenRoute(tokenType, isTempRoute)
            println("[JwtFilter] Token route validation passed")

            validateUserStatus(tokenType, tokenData)
            println("[JwtFilter] User status validation passed")

            val authorities = buildAuthorities(tokenType, tokenData)
            println("[JwtFilter] Authorities built: $authorities")

            SecurityContextHolder.getContext().authentication =
                UsernamePasswordAuthenticationToken(tokenData, null, authorities)
            println("[JwtFilter] SecurityContext set with authentication")

            filterChain.doFilter(request, response)
            println("[JwtFilter] Filter chain continued successfully")

        } catch (ex: Exception) {
            println("[JwtFilter] Exception caught: ${ex.message}")
            ex.printStackTrace()
            SecurityContextHolder.clearContext()
            println("[JwtFilter] SecurityContext cleared")
            handlerExceptionResolver.resolveException(request, response, null, ex)
            println("[JwtFilter] Exception passed to handlerExceptionResolver")
        }
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization")
        println("[JwtFilter] Authorization header: $header")
        if (header == null || !header.startsWith("Bearer ")) return null
        return header.removePrefix("Bearer ").trim()
    }

    private fun isTempRoute(method: String, path: String): Boolean {
        val result = tempOnlyRoutes.any { rule -> (rule.method == "ANY" || rule.method == method) && pathMatcher.match(rule.pattern, path) }
        println("[JwtFilter] isTempRoute check for path=$path, method=$method: $result")
        return result
    }

    private fun extractTokenData(token: String, tokenType: String): TokenData {
        println("[JwtFilter] extractTokenData - tokenType: $tokenType")
        return when (tokenType) {
            JwtService.TOKEN_TYPE_ACCESS -> jwtService.extractAccessTokenData(token)
            JwtService.TOKEN_TYPE_TEMP -> jwtService.extractTempTokenData(token)
            else -> throw BusinessException("Token inválido ou mal formado")
        } ?: throw BusinessException("Token inválido ou mal formado")
    }

    private fun validateTokenRoute(tokenType: String, isTempRoute: Boolean) {
        println("[JwtFilter] validateTokenRoute - tokenType: $tokenType, isTempRoute: $isTempRoute")
        if (isTempRoute && tokenType != JwtService.TOKEN_TYPE_TEMP)
            throw BusinessException("Apenas token temporário permitido")
        if (!isTempRoute && tokenType == JwtService.TOKEN_TYPE_TEMP)
            throw BusinessException("Token temporário não permitido nesta rota")
    }

    private fun validateUserStatus(tokenType: String, data: TokenData) {
        println("[JwtFilter] validateUserStatus - tokenType: $tokenType, data: $data")
        if (tokenType == JwtService.TOKEN_TYPE_ACCESS && data.role != "ADMIN" && data.status != "VERIFICADO")
            throw BusinessException("Usuário não verificado")
    }

    private fun buildAuthorities(tokenType: String, data: TokenData) = listOf(
        SimpleGrantedAuthority("TOKEN_$tokenType"),
        SimpleGrantedAuthority("ROLE_${data.role}"),
        SimpleGrantedAuthority("STATUS_${data.status}")
    ).also { println("[JwtFilter] buildAuthorities: $it") }

    data class RouteRule(val method: String, val pattern: String)
}