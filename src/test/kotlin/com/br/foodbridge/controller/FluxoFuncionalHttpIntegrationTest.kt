package com.br.foodbridge.controller

import com.br.foodbridge.domain.enums.*
import com.br.foodbridge.domain.model.*
import com.br.foodbridge.domain.repository.*
import com.br.foodbridge.service.utils.JwtService
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import java.time.LocalDateTime
import java.util.UUID

/** HTTP real, JWT e PostgreSQL efêmero. Não acessa a VM nem o banco de produção. */
@SpringBootTest
@AutoConfigureMockMvc
class FluxoFuncionalHttpIntegrationTest(
    @Autowired private val mvc: MockMvc,
    @Autowired private val mapper: ObjectMapper,
    @Autowired private val usuarios: UsuarioRepository,
    @Autowired private val organizacoes: OrganizacaoRepository,
    @Autowired private val vinculos: UsuarioOrganizacaoRepository,
    @Autowired private val jwt: JwtService,
    @Autowired private val encoder: PasswordEncoder
) {
    private val endereco = Endereco("Rua de teste", null, "10", "74000000", "Centro", "Goiania", "GO", "Brasil")
    private data class Conta(val usuario: Usuario, val org: Organizacao, val vinculo: UsuarioOrganizacao, val token: String)

    private fun conta(role: OrganizacaoRole): Conta {
        val unique = UUID.randomUUID().toString().replace("-", "")
        val usuario = usuarios.save(Usuario(nome = "Pessoa sintética", email = "$unique@example.invalid",
            senha = encoder.encode("senha-sintetica"), status = UserStatus.VERIFICADO))
        val org = organizacoes.save(Organizacao(nome = "Organização de teste", cnpj = unique.take(14),
            email = "org-$unique@example.invalid", telefone = "62999999999", endereco = endereco,
            role = role, status = StatusOrganizacao.VERIFICADO))
        val vinculo = vinculos.save(UsuarioOrganizacao(usuario = usuario, organizacao = org,
            role = role, status = StatusOrganizacao.VERIFICADO))
        return Conta(usuario, org, vinculo,
            jwt.generateAccessToken(usuario.id!!, org.id!!, vinculo.id!!, role.name, UserStatus.VERIFICADO))
    }

    private fun chamada(request: MockHttpServletRequestBuilder, token: String? = null,
                        body: Any? = null, expected: Int = 200): JsonNode {
        if (token != null) request.header("Authorization", "Bearer $token")
        if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))
        val response = mvc.perform(request).andReturn().response
        assertEquals(expected, response.status, response.contentAsString)
        return if (response.contentAsString.isBlank()) mapper.nodeFactory.nullNode()
            else mapper.readTree(response.contentAsString)
    }

    @Test
    fun loginSelecaoRetornaIdRealDoVinculoETokenValido() {
        val c = conta(OrganizacaoRole.ONG)
        val login = chamada(post("/auth/login"), body = mapOf("email" to c.usuario.email, "senha" to "senha-sintetica"))
        assertTrue(login["tempToken"].isTextual)
        val result = chamada(post("/auth/select-org"), login["tempToken"].asText(),
            mapOf("organizacaoId" to c.org.id))
        assertEquals(c.vinculo.id, result["vinculoId"].asLong())
        assertEquals(c.org.id, result["organizacaoId"].asLong())
        assertEquals(c.vinculo.id, jwt.extractAccessTokenData(result["token"].asText()).vinculoId)
        chamada(get("/usuarios/me"), result["token"].asText())
    }

    @Test
    fun revogacaoDeVinculoImpedeLeituraComJwtAindaAssinado() {
        val c = conta(OrganizacaoRole.ONG)
        chamada(get("/requisicoes/minhas"), c.token)
        vinculos.save(c.vinculo.copy(status = StatusOrganizacao.INATIVO))
        chamada(get("/requisicoes/minhas"), c.token, expected = 403)
    }

    @Test
    fun administradorSuspensoNaoAcessaRotasAdministrativas() {
        val c = conta(OrganizacaoRole.ADMIN)
        usuarios.save(c.usuario.copy(status = UserStatus.SUSPENSO))
        chamada(get("/admin/usuarios"), c.token, expected = 403)
        chamada(post("/auth/login"), body = mapOf("email" to c.usuario.email, "senha" to "senha-sintetica"), expected = 403)
    }

    @Test
    fun cadastroDeOrganizacaoValidaEnumSemErro500ESemAutoAprovacao() {
        val unique = UUID.randomUUID().toString().replace("-", "")
        val usuario = usuarios.save(Usuario(nome = "Solicitante", email = "$unique@example.invalid",
            senha = encoder.encode("senha"), status = UserStatus.PENDENTE_VERIFICACAO))
        val token = jwt.generateTempToken(usuario.id!!, UserStatus.PENDENTE_VERIFICACAO)
        val result = chamada(post("/organizacoes"), token, mapOf(
            "nome" to "Organização nova", "cnpj" to unique.take(14), "telefone" to "62999999999",
            "email" to "org-$unique@example.invalid", "role" to "ONG", "endereco" to endereco,
            "status" to "VERIFICADO"), 201)
        val org = organizacoes.findByCnpj(unique.take(14))!!
        assertEquals(StatusOrganizacao.REVISAO, org.status)
        assertFalse(result.toString().contains("senha"))
    }

    private fun cpf(): String {
        val seed = UUID.randomUUID().mostSignificantBits.toString().filter { it.isDigit() }.padEnd(9, '3').take(9)
        val digitos = seed.map { it.digitToInt() }.toMutableList()
        repeat(2) {
            val resto = digitos.mapIndexed { index, n -> n * (digitos.size + 1 - index) }.sum() % 11
            digitos.add(if (resto < 2) 0 else 11 - resto)
        }
        return digitos.joinToString("")
    }

    @Test
    fun cicloCompletoDeDoacaoSolicitacaoVoluntarioEColetaAtualizaIndicadores() {
        val doador = conta(OrganizacaoRole.DOADOR)
        val ong = conta(OrganizacaoRole.ONG)
        val doacao = chamada(post("/doacoes"), doador.token, mapOf(
            "tipoComida" to "FRUTAS", "descricaoComida" to "Frutas de teste", "quantidade" to 10,
            "unidade" to "KG", "dataExpiracao" to LocalDateTime.now().plusDays(3),
            "janelasDisponiveis" to listOf(mapOf("inicio" to LocalDateTime.now().plusDays(1),
                "fim" to LocalDateTime.now().plusDays(2))), "status" to "DISPONIVEL", "endereco" to endereco), 201)
        val req = chamada(post("/requisicoes"), ong.token,
            mapOf("doacaoId" to doacao["id"].asLong(), "quantidadeSolicitada" to 10), 201)
        val reqId = req["id"].asLong()
        chamada(patch("/requisicoes/$reqId/aprovar"), doador.token)
        val voluntario = chamada(post("/voluntarios"), ong.token, mapOf(
            "nome" to "Voluntário sintético", "telefone" to "62999999999",
            "email" to UUID.randomUUID().toString() + "@example.invalid",
            "cpf" to cpf(), "endereco" to endereco), 201)
        chamada(patch("/requisicoes/$reqId/vincular-voluntario"), ong.token,
            mapOf("voluntarioId" to voluntario["id"].asLong()))
        val concluida = chamada(patch("/requisicoes/$reqId/concluir"), doador.token)
        assertEquals("CONCLUIDO", concluida["status"].asText())
        assertEquals("COMPLETO", chamada(get("/doacoes/" + doacao["id"].asLong()), doador.token)["status"].asText())
        assertEquals(10.0, chamada(get("/dashboards/ong"), ong.token)["quantidadeRecebida"].asDouble())
        assertEquals(10.0, chamada(get("/dashboards/doador"), doador.token)["quantidadeDoada"].asDouble())
        chamada(patch("/requisicoes/$reqId/concluir"), doador.token, expected = 409)
    }

    @Test
    fun metodoIncorretoNoLoginRetorna405() {
        chamada(get("/auth/login"), expected = 405)
    }

    @Test
    fun jwtMalformadoRetorna401SemExporToken() {
        val result = chamada(get("/usuarios/me"), "token-sintetico-invalido", expected = 401)
        assertFalse(result.toString().contains("token-sintetico-invalido"))
    }
}
