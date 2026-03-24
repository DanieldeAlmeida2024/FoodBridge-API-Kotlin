package com.br.foodbridge.domain.model

import com.br.foodbridge.domain.enums.OrganizacaoRole
import com.br.foodbridge.domain.enums.StatusOrganizacao
import com.br.foodbridge.domain.enums.StatusVoluntario
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "usuario_organizacao",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["usuario_id", "organizacao_id"])
    ]
)
data class UsuarioOrganizacao(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    var usuario: Usuario? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizacao_id", nullable = false)
    @JsonIgnore
    var organizacao: Organizacao?=null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: OrganizacaoRole = OrganizacaoRole.DOADOR, // valor default

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: StatusOrganizacao = StatusOrganizacao.REVISAO,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = true)
    var approvedAt: LocalDateTime? = null
)