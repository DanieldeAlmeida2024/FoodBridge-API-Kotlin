package com.br.foodbridge.controller

import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.controller.dto.voluntario.CreateUpdateVoluntario
import com.br.foodbridge.controller.dto.voluntario.VoluntarioDTO
import com.br.foodbridge.domain.model.Voluntario
import com.br.foodbridge.service.VoluntarioService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/voluntarios")
class VoluntarioController (
    private val voluntarioService: VoluntarioService
){
    @PostMapping
    fun cadastrarOuVincular(
        authentication: org.springframework.security.core.Authentication,
        @Valid @RequestBody request: CreateUpdateVoluntario
    ): ResponseEntity<in VoluntarioDTO> {
        //Alterar
        val tokenData = authentication.principal as TokenData
        println(tokenData)
        println(tokenData::class)
        val voluntario = voluntarioService.criarOuVincular(request, tokenData)

        return ResponseEntity.ok(voluntario)
    }

    @GetMapping
    fun listar(): ResponseEntity<List<Voluntario>> {
        return ResponseEntity.ok(voluntarioService.findAll())
    }
}