package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.controller.dto.voluntario.CreateUpdateVoluntario
import com.br.foodbridge.domain.enums.StatusVoluntario
import com.br.foodbridge.domain.model.Voluntario
import com.br.foodbridge.domain.model.VoluntarioOrganizacao
import com.br.foodbridge.domain.repository.OrganizacaoRepository
import com.br.foodbridge.domain.repository.VoluntarioOrganizacaoRepository
import com.br.foodbridge.domain.repository.VoluntarioRepository
import org.springframework.stereotype.Service

@Service
class VoluntarioService(
    private val voluntarioRepository: VoluntarioRepository,
    private val voluntarioOrganizacaoRepository: VoluntarioOrganizacaoRepository,
    private val organizacaoRepository: OrganizacaoRepository
) {

    // CREATE
    fun criarOuVincular(voluntario: CreateUpdateVoluntario, tokenData: TokenData): Any {

        if (voluntarioRepository.existsByCpf(voluntario.cpf)) {
            val voluntario = voluntarioRepository.findByCpf(voluntario.cpf)
            if(voluntario.id != null) {
                if (voluntarioOrganizacaoRepository
                        .existsByVoluntarioIdAndOrganizacaoId(voluntario.id, tokenData.organizacaoId!!)) {
                    throw IllegalArgumentException("Já vinculado")
                }
                val organizacao = organizacaoRepository.findById(tokenData.organizacaoId)
                    .orElseThrow {IllegalArgumentException("Organização inválida")}
                val vinculo = VoluntarioOrganizacao(
                    voluntario = voluntario,
                    organizacao = organizacao,
                    status = StatusVoluntario.ATIVO
                )
                return voluntarioOrganizacaoRepository.save(vinculo)
            }
        }
        val entityVoluntario = Voluntario(
            nome = voluntario.nome,
            cpf = voluntario.cpf,
            email = voluntario.email,
            telefone = voluntario.telefone,
            endereco = voluntario.endereco
        )

        val voluntario = voluntarioRepository.save(entityVoluntario)
        val organizacao = organizacaoRepository.findById(tokenData.organizacaoId!!)
            .orElseThrow { IllegalArgumentException("Organização Inválida") }
        val vinculo = VoluntarioOrganizacao(
            voluntario = voluntario,
            organizacao = organizacao,
            status = StatusVoluntario.ATIVO
        )
        voluntarioOrganizacaoRepository.save(vinculo)
        return voluntario
    }

    // READ
    fun findById(id: Long): Voluntario {
        return voluntarioRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Voluntário não encontrado") }
    }

    fun findAll(): List<Voluntario> = voluntarioRepository.findAll()

    // UPDATE
    fun update(id: Long, updated: Voluntario): Voluntario {
        val v = findById(id)

        val novo = v.copy(
            nome = updated.nome,
            telefone = updated.telefone,
            endereco = updated.endereco
        )

        return voluntarioRepository.save(novo)
    }

    //implementar para que o usuário da organização não apague o voluntário, já que ele pode estar vinculado a outras ongs, apenas alterar o StatusVoluntario (INATIVO OU SUSPENSO)
}