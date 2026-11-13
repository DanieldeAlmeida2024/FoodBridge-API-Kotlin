package com.br.foodbridge.controller

import com.br.foodbridge.controller.dto.organizacao.CreateUpdateOrganizacaoRequest
import com.br.foodbridge.domain.enums.OrganizacaoRole
import com.br.foodbridge.domain.enums.StatusOrganizacao
import com.br.foodbridge.domain.enums.UserStatus
import com.br.foodbridge.domain.model.Endereco
import com.br.foodbridge.domain.model.Organizacao
import com.br.foodbridge.domain.model.Usuario
import com.br.foodbridge.domain.model.UsuarioOrganizacao
import com.br.foodbridge.domain.repository.OrganizacaoRepository
import com.br.foodbridge.domain.repository.UsuarioOrganizacaoRepository
import com.br.foodbridge.domain.repository.UsuarioRepository
import com.br.foodbridge.service.utils.JwtService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

/**
 * Integração HTTP + JwtFilter + Service + PostgreSQL.
 * Dados sintéticos; fixture persistida entre transações de teste.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrganizacaoAutorizacaoHttpIntegrationTest(
    @Autowired private val mvc: MockMvc,
    @Autowired private val mapper: ObjectMapper,
    @Autowired private val organizacoes: OrganizacaoRepository,
    @Autowired private val usuarios: UsuarioRepository,
    @Autowired private val vinculos: UsuarioOrganizacaoRepository,
    @Autowired private val jwt: JwtService
) {
    private val endereco = Endereco("Rua Fictícia", null, "100", "74000000",
        "Setor Teste", "Goiânia", "GO", "Brasil")

    private fun org(role: OrganizacaoRole = OrganizacaoRole.ONG): Organizacao {
        val unique = UUID.randomUUID().toString().replace("-", "").take(24)
        return organizacoes.save(Organizacao(
            nome = "Organização " + unique, cnpj = unique.take(14),
            email = unique + "@example.invalid", telefone = "62999999999",
            endereco = endereco, role = role, status = StatusOrganizacao.VERIFICADO
        ))
    }

    private data class Account(val org: Organizacao, val token: String, val vinculo: UsuarioOrganizacao)
    private fun account(
        role: OrganizacaoRole = OrganizacaoRole.ADMIN,
        vinculoStatus: StatusOrganizacao = StatusOrganizacao.VERIFICADO
    ): Account {
        val o = org(role)
        val email = "user-" + UUID.randomUUID() + "@example.invalid"
        val user = usuarios.save(Usuario(
            nome = "Pessoa sintética", email = email, senha = "hash-falso-nao-utilizado",
            status = UserStatus.VERIFICADO
        ))
        val vinculo = vinculos.save(UsuarioOrganizacao(
            usuario = user, organizacao = o, role = role, status = vinculoStatus
        ))
        val token = jwt.generateAccessToken(
            user.id!!, o.id!!, vinculo.id!!, role.name, UserStatus.VERIFICADO
        )
        return Account(o, token, vinculo)
    }

    private fun updateBody(org: Organizacao): String = mapper.writeValueAsString(
        CreateUpdateOrganizacaoRequest(
            nome = "Nome editado", cnpj = org.cnpj, description = null,
            telefone = org.telefone, email = org.email, website = null,
            role = org.role!!, status = StatusOrganizacao.REVISAO,
            endereco = endereco, verificationDate = null
        )
    )

    @Test
    fun putDeOutraOrganizacaoDevolve403ESemMutacao() {
        val admin = account()
        val target = org()
        mvc.perform(put("/organizacoes/" + target.id)
            .header("Authorization", "Bearer " + admin.token)
            .contentType(MediaType.APPLICATION_JSON).content(updateBody(target))
        ).andExpect(status().isForbidden)
        assertEquals(target.nome, organizacoes.findById(target.id!!).orElseThrow().nome)
    }

    @Test
    fun deleteDeOutraOrganizacaoDevolve403ESemInativar() {
        val admin = account()
        val target = org()
        mvc.perform(delete("/organizacoes/" + target.id)
            .header("Authorization", "Bearer " + admin.token)
        ).andExpect(status().isForbidden)
        assertEquals(StatusOrganizacao.VERIFICADO,
            organizacoes.findById(target.id!!).orElseThrow().status)
    }

    @Test
    fun deleteDaPropriaOrganizacaoVerificadaDevolve204() {
        val admin = account()
        mvc.perform(delete("/organizacoes/" + admin.org.id)
            .header("Authorization", "Bearer " + admin.token)
        ).andExpect(status().isNoContent)
        assertEquals(StatusOrganizacao.INATIVO,
            organizacoes.findById(admin.org.id!!).orElseThrow().status)
    }

    @Test
    fun tokenDeVinculoRevogadoNaoPodeInativarOrganizacao() {
        val admin = account(vinculoStatus = StatusOrganizacao.INATIVO)
        mvc.perform(delete("/organizacoes/" + admin.org.id)
            .header("Authorization", "Bearer " + admin.token)
        ).andExpect(status().isForbidden)
        assertEquals(StatusOrganizacao.VERIFICADO,
            organizacoes.findById(admin.org.id!!).orElseThrow().status)
    }

    @Test
    fun membroNaoAdminNaoPodeInativarOrganizacao() {
        val membro = account(role = OrganizacaoRole.ONG)
        mvc.perform(delete("/organizacoes/" + membro.org.id)
            .header("Authorization", "Bearer " + membro.token)
        ).andExpect(status().isForbidden)
    }

    @Test
    fun patchAprovacaoDeVinculoDeOutraOrganizacaoDevolve403() {
        val admin = account()
        val target = account(role = OrganizacaoRole.ONG, vinculoStatus = StatusOrganizacao.REVISAO)
        mvc.perform(patch("/organizacoes/usuarios/" + target.vinculo.id + "/aprovar")
            .header("Authorization", "Bearer " + admin.token)
        ).andExpect(status().isForbidden)
        assertEquals(StatusOrganizacao.REVISAO,
            vinculos.findById(target.vinculo.id!!).orElseThrow().status)
    }

    @Test
    fun patchReprovacaoDeVinculoDeOutraOrganizacaoDevolve403() {
        val admin = account()
        val target = account(role = OrganizacaoRole.ONG, vinculoStatus = StatusOrganizacao.REVISAO)
        mvc.perform(patch("/organizacoes/usuarios/" + target.vinculo.id + "/reprovar")
            .header("Authorization", "Bearer " + admin.token)
        ).andExpect(status().isForbidden)
        assertEquals(StatusOrganizacao.REVISAO,
            vinculos.findById(target.vinculo.id!!).orElseThrow().status)
    }
}
