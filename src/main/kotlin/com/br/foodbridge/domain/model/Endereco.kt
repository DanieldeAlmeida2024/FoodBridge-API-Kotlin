package com.br.foodbridge.domain.model

data class Endereco(
    val linha1: String,
    val linha2: String? = null,
    val numero: String,
    val cep: String,
    val bairro: String,
    val cidade: String,
    val estado: String,
    val pais: String
)
