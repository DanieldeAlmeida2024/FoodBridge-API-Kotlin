package com.br.foodbridge.domain.model

import com.br.foodbridge.domain.enums.StatusVoluntario

data class Voluntario(
    val id: Int,
    val Ongs: String,
    val nome: String,
    val telefone: String,
    val email: String,
    val endereco: String,
    val cpf: String,
    val status: StatusVoluntario
)
