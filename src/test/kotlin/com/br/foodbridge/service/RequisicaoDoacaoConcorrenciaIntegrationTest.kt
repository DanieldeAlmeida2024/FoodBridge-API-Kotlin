package com.br.foodbridge.service

import com.br.foodbridge.domain.enums.*
import com.br.foodbridge.domain.model.*
import com.br.foodbridge.domain.repository.*
import com.br.foodbridge.exception.custom.BusinessException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * R05 / REQ-15 — teste de concorrência real, contra PostgreSQL do GitHub Actions.
 * Não usar @Transactional no teste: cada worker precisa de transação própria.
 */
@SpringBootTest
class RequisicaoDoacaoConcorrenciaIntegrationTest(
    @Autowired private val service: RequisicaoDoacaoService,
    @Autowired private val organizacoes: OrganizacaoRepository,
    @Autowired private val doacoes: DoacaoRepository,
    @Autowired private val requisicoes: RequisicaoDoacaoRepository
) {
    private val endereco = Endereco("Rua Teste", "", "10", "74000000", "Setor Teste", "Goiânia", "GO", "Brasil")

    private fun org(tipo: OrganizacaoRole): Organizacao {
        val unique = UUID.randomUUID().toString().replace("-", "").take(24)
        return organizacoes.save(
            Organizacao(
                nome = "Organização $unique", cnpj = unique.take(14),
                email = "$unique@example.invalid", telefone = "62900000000",
                role = tipo, endereco = endereco, status = StatusOrganizacao.VERIFICADO
            )
        )
    }

    @Test
    fun duasOngsNaoPodemAprovarQuatorzeQuilosDeEstoqueDeDez() {
        val doador = org(OrganizacaoRole.DOADOR)
        val ongA = org(OrganizacaoRole.ONG)
        val ongB = org(OrganizacaoRole.ONG)

        val doacao = doacoes.save(
            Doacao(
                quantidade = 10.0, unidade = Unidade.KG, tipoComida = TipoDoacao.OUTROS,
                dataExpiracao = LocalDateTime.now().plusDays(4),
                status = StatusDoacao.DISPONIVEL, endereco = endereco, organizacao = doador
            )
        )
        val requisicaoA = requisicoes.save(
            RequisicaoDoacao(
                doacao = doacao, organizacaoSolicitante = ongA,
                quantidadeSolicitada = 7.0, status = StatusReivindicacao.PENDENTE
            )
        )
        val requisicaoB = requisicoes.save(
            RequisicaoDoacao(
                doacao = doacao, organizacaoSolicitante = ongB,
                quantidadeSolicitada = 7.0, status = StatusReivindicacao.PENDENTE
            )
        )

        val pool = Executors.newFixedThreadPool(2)
        val partida = CountDownLatch(1)
        try {
            val futures = listOf(requisicaoA, requisicaoB).map { req ->
                pool.submit(Callable {
                    partida.await(10, TimeUnit.SECONDS)
                    runCatching { service.aprovar(req.id, doador.id, OrganizacaoRole.DOADOR.name) }
                })
            }
            partida.countDown()
            val resultados = futures.map { it.get(30, TimeUnit.SECONDS) }
            assertEquals(1, resultados.count { it.isSuccess }, "Apenas uma ONG deve reservar 7/10 kg")
            assertEquals(1, resultados.count { it.exceptionOrNull() is BusinessException },
                "A segunda requisição deve falhar pela regra de estoque")

            val atualizadas = requisicoes.findByDoacao(doacoes.findById(doacao.id!!).orElseThrow())
            val aprovadas = atualizadas.filter { it.status == StatusReivindicacao.APROVADO }
            val pendentes = atualizadas.filter { it.status == StatusReivindicacao.PENDENTE }
            assertEquals(1, aprovadas.size)
            assertEquals(1, pendentes.size)
            assertEquals(7.0, aprovadas.sumOf { it.quantidadeSolicitada }, 0.001)
            assertTrue(aprovadas.sumOf { it.quantidadeSolicitada } <= doacao.quantidade)
        } finally {
            pool.shutdownNow()
        }
    }
}
