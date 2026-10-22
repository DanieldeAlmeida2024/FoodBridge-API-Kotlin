package com.br.foodbridge.service

import com.br.foodbridge.domain.enums.StatusDoacao
import com.br.foodbridge.domain.enums.StatusReivindicacao
import com.br.foodbridge.domain.enums.TipoDoacao
import com.br.foodbridge.domain.enums.Unidade
import com.br.foodbridge.domain.model.Doacao
import com.br.foodbridge.domain.model.Organizacao
import com.br.foodbridge.domain.model.RequisicaoDoacao
import com.br.foodbridge.domain.model.Voluntario
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImpactoAlimentarCalculatorTest {
    private val calculator = ImpactoAlimentarCalculator()

    private fun doacao(id: Long, unidade: Unidade = Unidade.KG, tipo: TipoDoacao = TipoDoacao.FRUTAS) =
        Doacao(id = id, unidade = unidade, tipoComida = tipo, quantidade = 100.0)

    private fun coleta(id: Long, doacao: Doacao, quantidade: Double, ong: Long = 2) =
        RequisicaoDoacao(
            id = id, doacao = doacao, quantidadeSolicitada = quantidade,
            organizacaoSolicitante = Organizacao(id = ong),
            voluntario = Voluntario(id = 1), status = StatusReivindicacao.CONCLUIDO
        )

    @Test
    fun `soma somente coletas concluidas preservando as cinco unidades`() {
        val frutas = doacao(1)
        val coletas = listOf(
            coleta(1, frutas, 12.5),
            coleta(2, frutas, 7.5),
            coleta(3, doacao(2, Unidade.LITRO), 15.0),
            coleta(4, doacao(3, Unidade.CAIXA), 4.0),
            coleta(5, doacao(4, Unidade.UNIDADE), 30.0),
            coleta(6, doacao(5, Unidade.LOTE), 2.0)
        ) + listOf(StatusReivindicacao.PENDENTE, StatusReivindicacao.APROVADO, StatusReivindicacao.CANCELADO, StatusReivindicacao.REJEITADO)
            .mapIndexed { i, status -> coleta(10L + i, frutas, 90.0).copy(status = status) }
        val impacto = calculator.calcular(coletas)
        assertEquals(mapOf(Unidade.KG to 20.0, Unidade.LITRO to 15.0, Unidade.UNIDADE to 30.0, Unidade.CAIXA to 4.0, Unidade.LOTE to 2.0), impacto.quantidadesPorUnidade)
        assertEquals(6, impacto.coletasConcluidas)
        assertEquals(5, impacto.doacoesAproveitadas)
    }

    @Test
    fun `coletas parciais contam sem multiplicar doacao ong ou voluntario`() {
        val frutas = doacao(1)
        val vegetais = doacao(2, tipo = TipoDoacao.VEGETAIS)
        val impacto = calculator.calcular(listOf(coleta(1, frutas, 10.0), coleta(2, frutas, 5.0), coleta(3, vegetais, 8.0, 3)))
        assertEquals(23.0, impacto.quantidadesPorUnidade[Unidade.KG])
        assertEquals(2, impacto.doacoesAproveitadas)
        assertEquals(2, impacto.organizacoesBeneficiadas)
        assertEquals(1, impacto.voluntariosParticipantes)
        assertEquals(2, impacto.categoriasDeAlimentos)
        assertEquals(15.0, impacto.porCategoria.first { it.tipoComida == TipoDoacao.FRUTAS }.quantidade)
    }

    @Test
    fun `fechamento manual acrescenta somente saldo ainda nao registrado nas coletas`() {
        val encerrada = doacao(1).copy(status = StatusDoacao.COMPLETO, quantidadeColetadaFinal = 80.0)
        val impacto = calculator.calcular(listOf(coleta(1, encerrada, 30.0)), listOf(encerrada))
        assertEquals(80.0, impacto.quantidadesPorUnidade[Unidade.KG])
        assertEquals(1, impacto.doacoesAproveitadas)
        assertEquals(1, impacto.coletasConcluidas)
        assertEquals(1, impacto.fechamentosManuais)
    }

    @Test
    fun `fechamento nao duplica coletas nem estima volume pela quantidade ofertada`() {
        val encerrada = doacao(1).copy(status = StatusDoacao.COMPLETO, quantidadeColetadaFinal = 30.0)
        val semQuantidadeConfirmada = doacao(2).copy(status = StatusDoacao.COMPLETO)
        val semColeta = doacao(3).copy(status = StatusDoacao.COMPLETO, quantidadeColetadaFinal = 0.0)
        val impacto = calculator.calcular(listOf(coleta(1, encerrada, 30.0)), listOf(encerrada, semQuantidadeConfirmada, semColeta))
        assertEquals(30.0, impacto.quantidadesPorUnidade[Unidade.KG])
        assertEquals(1, impacto.doacoesAproveitadas)
        assertEquals(0, impacto.fechamentosManuais)
    }

    @Test
    fun `quantidade confirmada sem requisicao conta sem inventar ong ou voluntario`() {
        val encerrada = doacao(1, Unidade.LITRO).copy(status = StatusDoacao.COMPLETO, quantidadeColetadaFinal = 18.0)
        val impacto = calculator.calcular(emptyList(), listOf(encerrada))
        assertEquals(18.0, impacto.quantidadesPorUnidade[Unidade.LITRO])
        assertEquals(1, impacto.doacoesAproveitadas)
        assertEquals(0, impacto.coletasConcluidas)
        assertEquals(0, impacto.organizacoesBeneficiadas)
        assertEquals(0, impacto.voluntariosParticipantes)
    }

    @Test
    fun `resultado da ong considera apenas suas coletas sem herdar fechamento da doadora`() {
        val encerrada = doacao(1).copy(status = StatusDoacao.COMPLETO, quantidadeColetadaFinal = 100.0)
        val impacto = calculator.calcular(listOf(coleta(1, encerrada, 25.0)))
        assertEquals(25.0, impacto.quantidadesPorUnidade[Unidade.KG])
        assertEquals(0, impacto.fechamentosManuais)
    }

    @Test
    fun `resultado vazio retorna todas as unidades com zero`() {
        val impacto = calculator.calcular(emptyList())
        assertEquals(Unidade.entries.toSet(), impacto.quantidadesPorUnidade.keys)
        assertTrue(impacto.quantidadesPorUnidade.values.all { it == 0.0 })
        assertEquals(0, impacto.coletasConcluidas)
        assertEquals(0, impacto.categoriasDeAlimentos)
        assertFalse(impacto.incluiDadosDemonstrativos)
    }

    @Test
    fun `dados demonstrativos sao sinalizados apenas quando compoem o aproveitamento`() {
        val demo = doacao(1).copy(descricaoComida = "[DEMO ESCOLAR 22/10/2026] Frutas")
        val pendente = coleta(1, demo, 10.0).copy(status = StatusReivindicacao.PENDENTE)
        assertFalse(calculator.calcular(listOf(pendente)).incluiDadosDemonstrativos)
        assertTrue(calculator.calcular(listOf(pendente.copy(status = StatusReivindicacao.CONCLUIDO))).incluiDadosDemonstrativos)
        assertTrue(calculator.calcular(listOf(coleta(2, doacao(2), 10.0).copy(observacao = "[DEMO ESCOLAR] Coleta"))).incluiDadosDemonstrativos)
    }
}
