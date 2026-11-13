package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.auth.TokenData
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.springframework.security.access.AccessDeniedException
import java.util.Optional

class OrganizacaoServiceAutorizacaoTest {
    private val organizacoes = mock(OrganizacaoRepository::class.java)
    private val vinculos = mock(UsuarioOrganizacaoRepository::class.java)
    private val service = OrganizacaoService(organizacoes, vinculos)

    private val endereco = Endereco("Rua de teste", null, "1", "74000000", "Centro", "Goiânia", "GO", "Brasil")
    private fun org(id: Long = 10L) = Organizacao(
        id = id, nome = "Organização de teste", cnpj = "00000000000000",
        telefone = "62900000000", email = "teste@exemplo.invalid",
        role = OrganizacaoRole.ADMIN, endereco = endereco, status = StatusOrganizacao.VERIFICADO
    )
    private fun token(
        orgId: Long? = 10L, role: String? = "ADMIN", vinculoId: Long? = 30L
    ) = TokenData(
        token = "fake-test-token", usuarioId = 7L, vinculoId = vinculoId,
        organizacaoId = orgId, role = role, status = "VERIFICADO"
    )
    private fun request() = CreateUpdateOrganizacaoRequest(
        nome = "Nome atualizado", cnpj = "00000000000000", description = null,
        telefone = "62911111111", email = "teste@exemplo.invalid", website = null,
        role = OrganizacaoRole.ADMIN, endereco = endereco, verificationDate = null
    )
    private fun vinculo(
        organization: Organizacao = org(),
        status: StatusOrganizacao = StatusOrganizacao.VERIFICADO
    ) = UsuarioOrganizacao(
        id = 30L, usuario = Usuario(id = 7L, status = UserStatus.VERIFICADO),
        organizacao = organization, role = OrganizacaoRole.ADMIN, status = status
    )
    private fun autorizar(vinculo: UsuarioOrganizacao = vinculo()) {
        `when`(vinculos.findByUsuarioIdAndOrganizacaoId(7L, 10L)).thenReturn(vinculo)
    }

    @Test
    fun usuarioDeOutraOrganizacaoNaoAlteraCadastro() {
        assertThrows(AccessDeniedException::class.java) {
            service.update(20L, request(), token())
        }
        verify(organizacoes, never()).findById(20L)
    }

    @Test
    fun membroSemPapelAdministrativoNaoAlteraCadastro() {
        assertThrows(AccessDeniedException::class.java) {
            service.update(10L, request(), token(role = "DOADOR"))
        }
        verify(organizacoes, never()).findById(10L)
    }

    @Test
    fun vinculoRevogadoInvalidaTokenAindaAssinado() {
        autorizar(vinculo(status = StatusOrganizacao.INATIVO))
        assertThrows(AccessDeniedException::class.java) {
            service.update(10L, request(), token())
        }
        verify(organizacoes, never()).findById(10L)
    }

    @Test
    fun administradorVerificadoAlteraApenasSuaOrganizacao() {
        val original = org()
        autorizar()
        `when`(organizacoes.findById(10L)).thenReturn(Optional.of(original))
        `when`(organizacoes.save(any(Organizacao::class.java))).thenAnswer { it.arguments[0] }

        val atualizado = service.update(10L, request(), token())

        assertEquals("Nome atualizado", atualizado.nome)
        verify(organizacoes).save(any(Organizacao::class.java))
    }

    @Test
    fun administradorNaoPodeAprovarVinculoDeOutraOrganizacao() {
        autorizar()
        `when`(vinculos.findById(99L)).thenReturn(Optional.of(vinculo(org(20L))))
        assertThrows(AccessDeniedException::class.java) {
            service.aprovarUsuarioOrganizacao(99L, token())
        }
        verify(vinculos, never()).save(any(UsuarioOrganizacao::class.java))
    }
}
