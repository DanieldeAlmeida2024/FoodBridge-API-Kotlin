package com.br.foodbridge.domain.model

import com.br.foodbridge.controller.dto.usuario.UsuarioOrganizacaoDTO
import com.br.foodbridge.domain.enums.OrganizacaoRole
import com.br.foodbridge.domain.enums.StatusOrganizacao
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "organizacoes")
data class Organizacao(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val nome: String = "",

    @Column(nullable = false, unique = true)
    val cnpj: String = "",

    @Column(nullable = true)
    val description: String? = null,

    @Column(nullable = false)
    val telefone: String = "",

    @Column(nullable = false, unique = true)
    val email: String = "",

    @Column(nullable = true)
    var role: OrganizacaoRole? = null,

    @Column(nullable = true)
    val website: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: StatusOrganizacao = StatusOrganizacao.DOCUMENTOS_PENDENTES,

    @Column(nullable = true)
    val verificationDate: LocalDateTime? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "organizacao", fetch = FetchType.LAZY)
    val usuarios: MutableList<UsuarioOrganizacao> = mutableListOf(),

    @OneToMany(mappedBy = "organizacao", fetch = FetchType.LAZY)
    val voluntarios: MutableList<VoluntarioOrganizacao> = mutableListOf()
)
