package com.br.foodbridge.middleware

import com.br.foodbridge.service.utils.JwtService
import com.br.foodbridge.service.ContextoAcessoService
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.servlet.HandlerExceptionResolver

class JwtFilterRotasPublicasTest {
    private val filtro = JwtFilter(
        mock(JwtService::class.java),
        mock(HandlerExceptionResolver::class.java),
        mock(ContextoAcessoService::class.java)
    )

    private fun verificarRotaPublica(metodo: String, caminho: String) {
        val requisicao = MockHttpServletRequest(metodo, caminho)
        requisicao.addHeader("Authorization", "Bearer token-invalido-de-teste")
        val cadeia = MockFilterChain()
        filtro.doFilter(requisicao, MockHttpServletResponse(), cadeia)
        assertNotNull(cadeia.request)
    }

    @Test
    fun cadastroDeUsuarioPermiteAcessoSemValidacaoDoToken() {
        verificarRotaPublica("POST", "/usuarios")
    }

    @Test
    fun consultaPublicaPermiteAcessoSemValidacaoDoToken() {
        verificarRotaPublica("GET", "/dashboards/publico")
    }

    @Test
    fun livenessPermiteAcessoSemValidacaoDoToken() {
        verificarRotaPublica("GET", "/health/live")
    }

    @Test
    fun loginPermiteAcessoSemValidacaoDoToken() {
        verificarRotaPublica("POST", "/auth/login")
    }
}
