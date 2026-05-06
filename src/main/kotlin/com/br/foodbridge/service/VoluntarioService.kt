package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.controller.dto.voluntario.CreateUpdateVoluntario
import com.br.foodbridge.domain.enums.StatusVoluntario
import com.br.foodbridge.domain.model.Endereco
import com.br.foodbridge.domain.model.Voluntario
import com.br.foodbridge.domain.model.VoluntarioOrganizacao
import com.br.foodbridge.domain.repository.OrganizacaoRepository
import com.br.foodbridge.domain.repository.VoluntarioOrganizacaoRepository
import com.br.foodbridge.domain.repository.VoluntarioRepository
import com.br.foodbridge.exception.custom.BusinessException
import com.br.foodbridge.exception.custom.ResourceNotFoundException
import com.br.foodbridge.exception.custom.ValidationException
import org.springframework.stereotype.Service

@Service
class VoluntarioService(
    private val voluntarioRepository: VoluntarioRepository,
    private val voluntarioOrganizacaoRepository: VoluntarioOrganizacaoRepository,
    private val organizacaoRepository: OrganizacaoRepository
) {

    fun criarOuVincular(request: CreateUpdateVoluntario, tokenData: TokenData): Voluntario {

        val organizacaoId = tokenData.organizacaoId
            ?: throw ValidationException("Organização do token é obrigatória")

        if (request.cpf.isBlank()) {
            throw ValidationException("CPF é obrigatório")
        }

        if (request.nome.isBlank()) {
            throw ValidationException("Nome é obrigatório")
        }

        val organizacao = organizacaoRepository.findById(organizacaoId)
            .orElseThrow { ResourceNotFoundException("Organização não encontrada") }

        val voluntarioExistente = voluntarioRepository.findByCpf(request.cpf)

        if (voluntarioExistente != null) {

            if (voluntarioExistente.id == null) {
                throw BusinessException("Voluntário inválido")
            }

            val jaVinculado = voluntarioOrganizacaoRepository
                .existsByVoluntarioIdAndOrganizacaoId(
                    voluntarioExistente.id,
                    organizacaoId
                )

            if (jaVinculado) {
                throw BusinessException("Voluntário já vinculado a esta organização")
            }

            val vinculo = VoluntarioOrganizacao(
                voluntario = voluntarioExistente,
                organizacao = organizacao,
                status = StatusVoluntario.ATIVO
            )

            voluntarioOrganizacaoRepository.save(vinculo)

            return voluntarioExistente
        }

        if (voluntarioRepository.existsByEmail(request.email)) {
            throw BusinessException("Email ja cadastrado para outro voluntario")
        }

        val endereco = Endereco(
            linha1 = request.endereco.linha1,
            linha2 = request.endereco.linha2,
            numero = request.endereco.numero,
            cep = request.endereco.cep,
            bairro = request.endereco.bairro,
            cidade = request.endereco.cidade,
            estado = request.endereco.estado,
            pais = request.endereco.pais
        )

        val novoVoluntario = Voluntario(
            nome = request.nome,
            cpf = request.cpf,
            email = request.email,
            telefone = request.telefone,
            endereco = endereco
        )

        val salvo = voluntarioRepository.save(novoVoluntario)

        val vinculo = VoluntarioOrganizacao(
            voluntario = salvo,
            organizacao = organizacao,
            status = StatusVoluntario.ATIVO
        )

        voluntarioOrganizacaoRepository.save(vinculo)

        return salvo
    }

    fun findById(id: Long?): Voluntario {

        if (id == null || id <= 0) {
            throw ValidationException("ID do voluntário inválido")
        }

        return voluntarioRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Voluntário não encontrado") }
    }

    fun findAll(): List<Voluntario> = voluntarioRepository.findAll()

    fun findByCpf(cpf: String?): Voluntario {
        if (cpf.isNullOrBlank()) {
            throw ValidationException("CPF do voluntario e obrigatorio")
        }

        return voluntarioRepository.findByCpf(cpf)
            ?: throw ResourceNotFoundException("Voluntario nao encontrado")
    }

    fun update(id: Long, request: CreateUpdateVoluntario): Voluntario {

        val voluntario = findById(id)

        val atualizado = voluntario.copy(
            nome = request.nome,
            telefone = request.telefone,
            email = request.email,
            endereco = voluntario.endereco.copy(
                linha1 = request.endereco.linha1,
                linha2 = request.endereco.linha2,
                numero = request.endereco.numero,
                cep = request.endereco.cep,
                bairro = request.endereco.bairro,
                cidade = request.endereco.cidade,
                estado = request.endereco.estado,
                pais = request.endereco.pais
            )
        )

        return voluntarioRepository.save(atualizado)
    }

    fun desativarVinculo(voluntarioId: Long, organizacaoId: Long) {

        if (voluntarioId <= 0 || organizacaoId <= 0) {
            throw ValidationException("IDs inválidos")
        }

        val vinculo = voluntarioOrganizacaoRepository
            .findByVoluntarioIdAndOrganizacaoId(voluntarioId, organizacaoId)
            ?: throw ResourceNotFoundException("Vínculo não encontrado")

        val atualizado = vinculo.copy(
            status = StatusVoluntario.INATIVO
        )

        voluntarioOrganizacaoRepository.save(atualizado)
    }
}
