package com.br.foodbridge.controller

import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Liveness HTTP leve para GitHub Actions e systemd; nao consulta PostgreSQL.
 * Ready funcional permanece validado em /dashboards/publico.
 */
@RestController
@Profile("!schema-validation")
class HealthController {
    @GetMapping("/health/live")
    fun live(): ResponseEntity<Map<String, String>> =
        ResponseEntity.ok(mapOf("status" to "UP"))
}
