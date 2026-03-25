package com.br.foodbridge.controller

import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.controller.dto.doacao.DoacaoDTO
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/doacao")
class DoacaoController {
    @PostMapping("/publicar")
    fun publicar(
        @AuthenticationPrincipal tokenData: TokenData,
        @RequestBody @Valid doacao: DoacaoDTO
    ){

    }
}