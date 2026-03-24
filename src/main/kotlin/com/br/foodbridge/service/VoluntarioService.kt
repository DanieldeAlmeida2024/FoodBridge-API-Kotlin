package com.br.foodbridge.service

import com.br.foodbridge.domain.model.Organizacao
import com.br.foodbridge.domain.model.Voluntario
import com.br.foodbridge.domain.model.VoluntarioOrganizacao
import com.br.foodbridge.domain.repository.VoluntarioOrganizacaoRepository
import com.br.foodbridge.domain.repository.VoluntarioRepository
import org.springframework.stereotype.Service

@Service
class VoluntarioService(
    private val voluntarioRepository: VoluntarioRepository,
    private val voluntarioOrganizacaoRepository: VoluntarioOrganizacaoRepository
) {

    // CREATE
    fun create(voluntario: Voluntario): Voluntario {

        if (voluntarioRepository.existsByCpf(voluntario.cpf)) {
            throw IllegalArgumentException("CPF já cadastrado")
        }

        return voluntarioRepository.save(voluntario)
    }

    // VINCULAR A ORGANIZAÇÃO
    fun vincular(voluntarioId: Long, organizacao: Organizacao): VoluntarioOrganizacao {

        if (voluntarioOrganizacaoRepository
                .existsByVoluntarioIdAndOrganizacaoId(voluntarioId, organizacao.id!!)) {
            throw IllegalArgumentException("Já vinculado")
        }

        val voluntario = voluntarioRepository.findById(voluntarioId)
            .orElseThrow { IllegalArgumentException("Voluntário não encontrado") }

        val vinculo = VoluntarioOrganizacao(
            voluntario = voluntario,
            organizacao = organizacao
        )

        return voluntarioOrganizacaoRepository.save(vinculo)
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