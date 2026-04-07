package com.br.foodbridge.exception.response

import java.time.LocalDateTime

data class ErrorResponse(
    val mensagem: String?,
     val codigo: Int,
     val timestamp: LocalDateTime = LocalDateTime.now()
)
