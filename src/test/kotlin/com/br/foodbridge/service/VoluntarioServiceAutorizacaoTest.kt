package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.controller.dto.voluntario.CreateUpdateVoluntario
import com.br.foodbridge.domain.enums.OrganizacaoRole
import com.br.foodbridge.domain.enums.StatusOrganizacao
import com.br.foodbridge.domain.enums.StatusVoluntario
import com.br.foodbridge.domain.enums.UserStatus
import com.br.foodbridge.domain.model.Endereco
import com.br.foodbridge.domain.model.Organizacao
import com.br.foodbridge.domain.model.Usuario
import com.br.foodbridge.domain.model.UsuarioOrganizacao
import com.br.foodbridge.domain.model.Voluntario
import com.br.foodbridge.domain.model.VoluntarioOrganizacao
import com.br.foodbridge.domain.repository.OrganizacaoRepository
import com.br.foodbridge.domain.repository.UsuarioOrganizacaoRepository
import com.br.foodbridge.domain.repository.VoluntarioOrganizacaoRepository
import com.br.foodbridge.domain.repository.VoluntarioRepository
import com.br.foodbridge.exception.custom.ResourceNotFoundException
import com.br.foodbridge.exception.custom.BusinessException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.access.AccessDeniedException
import java.util.Optional

class VoluntarioServiceAutorizacaoTest {
    private val voluntarios = mock(VoluntarioRepository::class.java)
    private val vinculosVoluntario = mock(VoluntarioOrganizacaoRepository::class.java)
    private val organizacoes = mock(OrganizacaoRepository::class.java)
    private val vinculosUsuario = mock(UsuarioOrganizacaoRepository::class.java)
    private val service = VoluntarioService(voluntarios, vinculosVoluntario, organizacoes, vinculosUsuario)
    private val endereco = Endereco("Rua Exemplo", null, "10", "74000000", "Centro", "Goiânia", "GO", "Brasil")

    private fun org(id: Long) = Organizacao(
        id = id, nome = "ONG $id", cnpj = "00000000000000", telefone = "62999999999",
        email = "ong$id@example.invalid", role = OrganizacaoRole.ONG,
        status = StatusOrganizacao.VERIFICADO, endereco = endereco
    )
    private fun voluntario(id: Long = 50L) = Voluntario(
        id = id, nome = "Pessoa de teste", cpf = "11144477735",
        telefone = "62999999999", email = "voluntario@example.invalid", endereco = endereco
    )
    private fun token(orgId: Long = 10L, vinculoId: Long = 30L) = TokenData(
        token = "token-falso", usuarioId = 7L, vinculoId = vinculoId,
        organizacaoId = orgId, role = "ONG", status = "VERIFICADO"
    )
    private fun vinculoUsuario(
        status: StatusOrganizacao = StatusOrganizacao.VERIFICADO,
        id: Long = 30L
    ) = UsuarioOrganizacao(
        id = id, usuario = Usuario(id = 7L, status = UserStatus.VERIFICADO),
        organizacao = org(10L), role = OrganizacaoRole.ONG, status = status
    )
    private fun vinculoVoluntario(
        idOrg: Long = 10L,
        status: StatusVoluntario = StatusVoluntario.ATIVO
    ) = VoluntarioOrganizacao(
        id = 60L, organizacao = org(idOrg), voluntario = voluntario(), status = status
    )
    private fun request() = CreateUpdateVoluntario(
        nome = "Pessoa atualizada", telefone = "62999999999",
        email = "voluntario@example.invalid", cpf = "11144477735", endereco = endereco
    )
    private fun autenticar(vinculo: UsuarioOrganizacao = vinculoUsuario()) {
        `when`(vinculosUsuario.findByUsuarioIdAndOrganizacaoId(7L, 10L)).thenReturn(vinculo)
    }
    private fun associar(status: StatusVoluntario = StatusVoluntario.ATIVO) {
        `when`(vinculosVoluntario.findByVoluntarioIdAndOrganizacaoId(50L, 10L))
            .thenReturn(vinculoVoluntario(status = status))
    }

    @Test
    fun consultaPorIdDeOutraOrganizacaoNaoRevelaDados() {
        autenticar()
        assertThrows(ResourceNotFoundException::class.java) {
            service.findById(50L, token())
        }
        verify(voluntarios, never()).findById(50L)
    }

    @Test
    fun consultaPorCpfDeOutraOrganizacaoNaoRevelaDados() {
        autenticar()
        `when`(voluntarios.findByCpf("11144477735")).thenReturn(voluntario())
        assertThrows(ResourceNotFoundException::class.java) {
            service.findByCpf("11144477735", token())
        }
    }

    @Test
    fun listarRetornaApenasVinculosAtivosDaOrganizacao() {
        autenticar()
        `when`(vinculosVoluntario.findAllByOrganizacaoId(10L)).thenReturn(
            listOf(vinculoVoluntario(), vinculoVoluntario(status = StatusVoluntario.INATIVO))
        )
        val resultado = service.findAll(token())
        assertEquals(1, resultado.size)
        assertEquals(50L, resultado.single().id)
        verify(voluntarios, never()).findAll()
    }

    @Test
    fun vinculoDeUsuarioRevogadoBloqueiaConsultas() {
        autenticar(vinculoUsuario(status = StatusOrganizacao.INATIVO))
        assertThrows(AccessDeniedException::class.java) {
            service.findAll(token())
        }
        verify(vinculosVoluntario, never()).findAllByOrganizacaoId(10L)
    }

    @Test
    fun cpfExistenteNaoCriaVinculoAutomaticoOutraOrganizacao() {
        autenticar()
        `when`(organizacoes.findById(10L)).thenReturn(Optional.of(org(10L)))
        `when`(voluntarios.existsByCpf("11144477735")).thenReturn(true)
        assertThrows(BusinessException::class.java) {
            service.criarOuVincular(request(), token())
        }
        verify(vinculosVoluntario, never()).save(any(VoluntarioOrganizacao::class.java))
    }

    @Test
    fun atualizarVoluntarioSemVinculoNaoPersisteAlteracao() {
        autenticar()
        assertThrows(ResourceNotFoundException::class.java) {
            service.update(50L, request(), token())
        }
        verify(voluntarios, never()).save(any(Voluntario::class.java))
    }

    @Test
    fun cadastroCompartilhadoNaoEhAlteradoPorUmSoTenant() {
        autenticar()
        associar()
        `when`(vinculosVoluntario.findAllByVoluntarioId(50L))
            .thenReturn(listOf(vinculoVoluntario(), vinculoVoluntario(idOrg = 20L)))
        assertThrows(AccessDeniedException::class.java) {
            service.update(50L, request(), token())
        }
        verify(voluntarios, never()).save(any(Voluntario::class.java))
    }

    @Test
    fun membroVerificadoPodeConsultarVoluntarioAtivoDaOrganizacao() {
        autenticar()
        associar()
        assertEquals(50L, service.findById(50L, token()).id)
    }

    @Test
    fun funcionarioDeOutraOrganizacaoNaoDesativaVinculo() {
        autenticar()
        assertThrows(ResourceNotFoundException::class.java) {
            service.desativarVinculo(50L, token())
        }
        verify(vinculosVoluntario, never()).save(any(VoluntarioOrganizacao::class.java))
    }
}
