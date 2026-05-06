package com.br.foodbridge.domain.model

import com.br.foodbridge.domain.enums.StatusReivindicacao
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
import java.time.LocalDateTime

@Entity
@Table(name = "requisicoes_doacao")
data class RequisicaoDoacao(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    val doacao: Doacao = Doacao(),

    @ManyToOne(fetch = FetchType.LAZY)
    val organizacaoSolicitante: Organizacao = Organizacao(),

    @ManyToOne(fetch = FetchType.LAZY)
    val voluntario: Voluntario? = null,

    @Column(nullable = false)
    val quantidadeSolicitada: Double = 0.0,

    @Column(nullable = true)
    val observacao: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: StatusReivindicacao = StatusReivindicacao.PENDENTE,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = true)
    val respondedAt: LocalDateTime? = null,

    @Column(nullable = true)
    val completedAt: LocalDateTime? = null
)
