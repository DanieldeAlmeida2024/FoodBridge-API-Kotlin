package com.br.foodbridge.domain.model

import com.br.foodbridge.controller.dto.usuario.UsuarioOrganizacaoDTO
import com.br.foodbridge.domain.enums.UserStatus
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.CascadeType
import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
@Table(name = "usuarios")
data class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var nome: String? = null,

    @Column(nullable = false, unique = true)
    var email: String? = null,

    @Column(nullable = false)
    @JsonIgnore
    var senha: String? = null,

    @OneToMany(mappedBy = "usuario", cascade = [CascadeType.ALL])
    @JsonIgnore
    var organizacoes: MutableList<UsuarioOrganizacao>? = mutableListOf(),

    @Column(nullable = false)
    var status: UserStatus? = UserStatus.PENDENTE_VERIFICACAO,

    @Column(nullable = true)
    var approvedAt: LocalDateTime? = LocalDateTime.now()
)