package com.br.foodbridge.controller.dto.auth

data class LoginRequest(
    val email: String,
    val senha: String
)