package com.br.foodbridge.controller

import com.br.foodbridge.domain.enums.*
import com.br.foodbridge.domain.model.*
import com.br.foodbridge.domain.repository.*
import com.br.foodbridge.service.utils.JwtService
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
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class PerfilHttpIntegrationTest(
    @Autowired private val mvc: MockMvc,
    @Autowired private val mapper: ObjectMapper,
    @Autowired private val usuarios: UsuarioRepository,
    @Autowired private val organizacoes: OrganizacaoRepository,
    @Autowired private val vinculos: UsuarioOrganizacaoRepository,
    @Autowired private val jwt: JwtService,
    @Autowired private val encoder: PasswordEncoder
) {
    private data class Conta(val usuario: Usuario, val orgId: Long, val token: String)
    private fun conta(): Conta {
        val unique = UUID.randomUUID().toString().replace("-", "")
        val user = usuarios.save(Usuario(nome = "Perfil sintético", email = "$unique@example.invalid",
            senha = encoder.encode("senha-antiga"), status = UserStatus.VERIFICADO))
        val org = organizacoes.save(Organizacao(nome = "ONG sintética", cnpj = unique.take(14),
            email = "org-$unique@example.invalid", telefone = "62999999999", role = OrganizacaoRole.ONG,
            status = StatusOrganizacao.VERIFICADO))
        val link = vinculos.save(UsuarioOrganizacao(usuario = user, organizacao = org,
            role = OrganizacaoRole.ONG, status = StatusOrganizacao.VERIFICADO))
        return Conta(user, org.id!!, jwt.generateAccessToken(user.id!!, org.id!!, link.id!!, "ONG", UserStatus.VERIFICADO))
    }
    private fun chamada(req: MockHttpServletRequestBuilder, token: String? = null, body: Any? = null, status: Int = 200): String {
        if (token != null) req.header("Authorization", "Bearer $token")
        if (body != null) req.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))
        val result = mvc.perform(req).andReturn().response
        assertEquals(status, result.status, result.contentAsString)
        return result.contentAsString
    }
    @Test
    fun cadastroValidoUsaBcryptENaoAceitaAutoAprovacao() {
        val email = UUID.randomUUID().toString() + "@example.invalid"
        val response = chamada(post("/usuarios"), body = mapOf("nome" to "Pessoa", "email" to email,
            "senha" to "senha-sintetica", "status" to "VERIFICADO"), status = 201)
        val saved = usuarios.findByEmail(email)!!
        assertEquals(UserStatus.PENDENTE_VERIFICACAO, saved.status)
        assertTrue(encoder.matches("senha-sintetica", saved.senha))
        assertFalse(response.contains("senha"))
        chamada(post("/usuarios"), body = mapOf("nome" to "Pessoa", "email" to email, "senha" to "outra"), status = 409)
    }
    @Test
    fun cadastroInvalidoRecusaNomeEmailESenha() {
        for (body in listOf(
            mapOf("nome" to "", "email" to "valido@example.invalid", "senha" to "senha"),
            mapOf("nome" to "Pessoa", "email" to "invalido", "senha" to "senha"),
            mapOf("nome" to "Pessoa", "email" to "valido@example.invalid", "senha" to "")
        )) chamada(post("/usuarios"), body = body, status = 400)
    }
    @Test
    fun alterarApenasNomePreservaEmailSenhaEStatus() {
        val c = conta()
        chamada(put("/usuarios/me"), c.token, mapOf("nome" to "Novo nome", "status" to "INATIVO"))
        val saved = usuarios.findById(c.usuario.id!!).orElseThrow()
        assertEquals("Novo nome", saved.nome)
        assertEquals(c.usuario.email, saved.email)
        assertEquals(c.usuario.senha, saved.senha)
        assertEquals(UserStatus.VERIFICADO, saved.status)
    }
    @Test
    fun emailDuplicadoNaoAlteraPerfil() {
        val c = conta()
        val outro = conta()
        chamada(put("/usuarios/me"), c.token, mapOf("email" to outro.usuario.email), 409)
        assertEquals(c.usuario.email, usuarios.findById(c.usuario.id!!).orElseThrow().email)
    }
    @Test
    fun trocaDeSenhaInvalidaSenhaAntigaEPermiteNova() {
        val c = conta()
        chamada(put("/usuarios/me"), c.token, mapOf("senha" to "senha-nova"))
        chamada(post("/auth/login"), body = mapOf("email" to c.usuario.email, "senha" to "senha-antiga"), status = 409)
        chamada(post("/auth/login"), body = mapOf("email" to c.usuario.email, "senha" to "senha-nova"))
        assertTrue(encoder.matches("senha-nova", usuarios.findById(c.usuario.id!!).orElseThrow().senha))
    }
    @Test
    fun usuarioComumNaoPodeExcluirContaPelaRotaAdministrativa() {
        val titular = conta()
        val terceiro = conta()
        chamada(delete("/usuarios/${terceiro.usuario.id!!}"), titular.token, status = 403)
        assertTrue(usuarios.existsById(terceiro.usuario.id!!))
    }
    @Test
    fun exclusaoRemoveContaEVinculosEPreservaOrganizacao() {
        val c = conta()
        chamada(delete("/usuarios/me"), c.token, status = 204)
        assertFalse(usuarios.existsById(c.usuario.id!!))
        assertTrue(vinculos.findAllByUsuarioId(c.usuario.id!!).isEmpty())
        assertTrue(organizacoes.existsById(c.orgId))
        chamada(get("/usuarios/me"), c.token, status = 403)
    }
}
