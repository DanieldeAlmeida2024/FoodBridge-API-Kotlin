package com.br.foodbridge.domain.model

import com.br.foodbridge.domain.enums.StatusOrganizacao
import org.springframework.format.annotation.DateTimeFormat


data class Organizacao(
    val id: Int,
    val nome: String,
    val cnpj: String,
    val description: String,
    val telefone: String,
    val email: String,
    val website: String,
    val status: StatusOrganizacao,
    val verificationDate: DateTimeFormat,
    val createdAt: DateTimeFormat,
)
