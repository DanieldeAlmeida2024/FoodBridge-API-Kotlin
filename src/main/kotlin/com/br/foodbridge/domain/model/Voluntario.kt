package com.br.foodbridge.domain.model

import com.br.foodbridge.domain.enums.StatusVoluntario
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import jakarta.validation.Valid
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime

@Entity
@Table(name = "voluntarios")
data class Voluntario(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val nome: String = "",

    @Column(nullable = false)
    val telefone: String = "",

    @Column(nullable = false, unique = true)
    val email: String = "",

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val endereco: Endereco = Endereco("","","","","","","",""),

    @Column(nullable = false, unique = true)
    val cpf: String = "",

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "voluntario", fetch = FetchType.LAZY)
    @JsonIgnore
    val organizacoes: MutableList<VoluntarioOrganizacao> = mutableListOf()
)
