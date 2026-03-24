package com.br.foodbridge.domain.model

import com.br.foodbridge.domain.enums.StatusVoluntario
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
@Table(
    name = "voluntario_organizacao",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["voluntario_id", "organizacao_id"])
    ]
)
data class VoluntarioOrganizacao(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voluntario_id", nullable = false)
    val voluntario: Voluntario? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizacao_id", nullable = false)
    @JsonIgnore
    val organizacao: Organizacao? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: StatusVoluntario = StatusVoluntario.ATIVO,


    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
