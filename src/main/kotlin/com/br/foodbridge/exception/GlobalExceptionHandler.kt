package com.br.foodbridge.exception

import com.br.foodbridge.exception.custom.BusinessException
import com.br.foodbridge.exception.custom.ResourceNotFoundException
import com.br.foodbridge.exception.custom.ValidationException
import com.br.foodbridge.exception.response.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(
                mensagem = ex.message,
                codigo = 404
            )
        )
    }

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<Map<String, String>> {
        val body = mapOf("error" to ex.message.orEmpty())
        return ResponseEntity(body, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidation(ex: ValidationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                mensagem = ex.message,
                codigo = 400
            )
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(
                mensagem = "Erro interno inesperado",
                codigo = 500
            )
        )
    }
}