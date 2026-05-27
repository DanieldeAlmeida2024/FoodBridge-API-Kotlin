package com.br.foodbridge.domain.model

import com.br.foodbridge.domain.enums.StatusDoacao
import com.br.foodbridge.domain.enums.TipoDoacao
import com.br.foodbridge.domain.enums.Unidade
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(name = "doacoes")
data class Doacao(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Enumerated(EnumType.STRING)
    val tipoComida: TipoDoacao = TipoDoacao.OUTROS,

    @Column(nullable = true)
    val descricaoComida: String = "",

    @Column(nullable = false)
    val quantidade: Double = 0.0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val unidade: Unidade = Unidade.UNIDADE,

    @Column(nullable = true)
    val dataExpiracao: LocalDateTime = LocalDateTime.now(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val janelasDisponiveis: List<JanelaHorario> = emptyList(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: StatusDoacao? = StatusDoacao.RASCUNHO,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val endereco: Endereco = Endereco("","","","","","","",""),

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    val organizacao: Organizacao = Organizacao(),

    @Column(nullable = true)
    val quantidadeColetadaFinal: Double? = null,

    @Column(nullable = true)
    val fechadoAt: LocalDateTime? = null,

    @Column(nullable = true)
    val observacaoFechamento: String? = null
)
