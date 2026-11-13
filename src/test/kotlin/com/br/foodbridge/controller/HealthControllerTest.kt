package com.br.foodbridge.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class HealthControllerTest {
    @Test
    fun livenessNaoConsultaBancoERetornaStatusHttp200() {
        val response = HealthController().live()
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("UP", response.body?.get("status"))
    }
}
