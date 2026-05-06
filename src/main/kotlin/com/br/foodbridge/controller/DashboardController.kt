package com.br.foodbridge.controller

import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.controller.dto.dashboard.DashboardDoadorDTO
import com.br.foodbridge.controller.dto.dashboard.DashboardOngDTO
import com.br.foodbridge.controller.dto.dashboard.DashboardPublicoDTO
import com.br.foodbridge.service.DashboardService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/dashboards")
class DashboardController(
    private val dashboardService: DashboardService
) {

    @GetMapping("/publico")
    fun publico(): ResponseEntity<DashboardPublicoDTO> =
        ResponseEntity.ok(dashboardService.publico())

    @GetMapping("/doador")
    fun doador(
        @AuthenticationPrincipal tokenData: TokenData
    ): ResponseEntity<DashboardDoadorDTO> =
        ResponseEntity.ok(dashboardService.doador(tokenData.organizacaoId, tokenData.role))

    @GetMapping("/ong")
    fun ong(
        @AuthenticationPrincipal tokenData: TokenData
    ): ResponseEntity<DashboardOngDTO> =
        ResponseEntity.ok(dashboardService.ong(tokenData.organizacaoId, tokenData.role))
}
