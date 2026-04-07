package com.br.foodbridge.controller.dto.mapper

import com.br.foodbridge.controller.dto.usuario.UsuarioResponse
import com.br.foodbridge.domain.model.Usuario

object UsuarioMapper {

    fun toResponse(usuario: Usuario): UsuarioResponse {
        return UsuarioResponse(
            id = usuario.id!!,
            nome = usuario.nome,
            email = usuario.email,
            status = usuario.status,
        )
    }
}