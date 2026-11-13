package com.br.foodbridge.service

import com.br.foodbridge.controller.dto.auth.TokenData
import com.br.foodbridge.controller.dto.voluntario.CreateUpdateVoluntario
import com.br.foodbridge.domain.enums.StatusOrganizacao
import com.br.foodbridge.domain.enums.StatusVoluntario
import com.br.foodbridge.domain.enums.UserStatus
import com.br.foodbridge.domain.model.Endereco
import com.br.foodbridge.domain.model.Voluntario
import com.br.foodbridge.domain.model.VoluntarioOrganizacao
import com.br.foodbridge.domain.repository.OrganizacaoRepository
import com.br.foodbridge.domain.repository.UsuarioOrganizacaoRepository
import com.br.foodbridge.domain.repository.VoluntarioOrganizacaoRepository
import com.br.foodbridge.domain.repository.VoluntarioRepository
import com.br.foodbridge.exception.custom.BusinessException
import com.br.foodbridge.exception.custom.ResourceNotFoundException
import com.br.foodbridge.exception.custom.ValidationException
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VoluntarioService(
    private val voluntarioRepository: VoluntarioRepository,
    private val voluntarioOrganizacaoRepository: VoluntarioOrganizacaoRepository,
    private val organizacaoRepository: OrganizacaoRepository,
    private val usuarioOrganizacaoRepository: UsuarioOrganizacaoRepository
) {

    /**
     * Revalida o vínculo do operador no banco em toda operação com dados pessoais.
     * Os claims assinados do JWT não garantem que o vínculo ainda esteja ativo.
     */
    private fun organizacaoAutorizada(tokenData: TokenData): Long {
        val organizacaoId = tokenData.organizacaoId
            ?: throw AccessDeniedException("Organização não informada")
        val vinculoId = tokenData.vinculoId
            ?: throw AccessDeniedException("Vínculo não informado")
        if (organizacaoId <= 0 || tokenData.status != UserStatus.VERIFICADO.name) {
            throw AccessDeniedException("Contexto de acesso inválido")
        }

        val vinculo = usuarioOrganizacaoRepository
            .findByUsuarioIdAndOrganizacaoId(tokenData.usuarioId, organizacaoId)
            ?: throw AccessDeniedException("Vínculo de usuário não encontrado")

        if (vinculo.id != vinculoId ||
            vinculo.status != StatusOrganizacao.VERIFICADO ||
            vinculo.role.name != tokenData.role ||
            vinculo.usuario?.status != UserStatus.VERIFICADO ||
            vinculo.organizacao?.status != StatusOrganizacao.VERIFICADO
        ) {
            throw AccessDeniedException("Vínculo de usuário não está vigente")
        }
        return organizacaoId
    }

    private fun voluntarioAutorizado(voluntarioId: Long?, organizacaoId: Long): Voluntario {
        if (voluntarioId == null || voluntarioId <= 0) {
            throw ValidationException("ID do voluntário inválido")
        }

        val vinculo = voluntarioOrganizacaoRepository
            .findByVoluntarioIdAndOrganizacaoId(voluntarioId, organizacaoId)
            ?.takeIf { it.status == StatusVoluntario.ATIVO }
            ?: throw ResourceNotFoundException("Voluntário não encontrado")

        return vinculo.voluntario
            ?: throw ResourceNotFoundException("Voluntário não encontrado")
    }

    @Transactional
    fun criarOuVincular(request: CreateUpdateVoluntario, tokenData: TokenData): Voluntario {
        val organizacaoId = organizacaoAutorizada(tokenData)

        if (request.cpf.isBlank()) {
            throw ValidationException("CPF é obrigatório")
        }
        if (request.nome.isBlank()) {
            throw ValidationException("Nome é obrigatório")
        }

        val organizacao = organizacaoRepository.findById(organizacaoId)
            .orElseThrow { ResourceNotFoundException("Organização não encontrada") }

        // Não compartilhar CPF, nome, contato ou endereço de outra organização,
        // nem criar um vínculo interorganizacional sem verificação de consentimento.
        // Uma jornada específica de autorização do voluntário deve ser implementada.
        if (voluntarioRepository.existsByCpf(request.cpf) ||
            voluntarioRepository.existsByEmail(request.email)
        ) {
            throw BusinessException("Cadastro não pode ser concluído; solicite validação do vínculo")
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

        val salvo = voluntarioRepository.save(
            Voluntario(
                nome = request.nome,
                cpf = request.cpf,
                email = request.email,
                telefone = request.telefone,
                endereco = endereco
            )
        )
        voluntarioOrganizacaoRepository.save(
            VoluntarioOrganizacao(
                voluntario = salvo,
                organizacao = organizacao,
                status = StatusVoluntario.ATIVO
            )
        )
        return salvo
    }

    @Transactional(readOnly = true)
    fun findById(id: Long?, tokenData: TokenData): Voluntario =
        voluntarioAutorizado(id, organizacaoAutorizada(tokenData))

    /**
     * Uso interno na coleta: a RequisicaoDoacaoService valida que a organização
     * solicitante pertence à requisição antes desta consulta.
     */
    @Transactional(readOnly = true)
    fun findByIdForColeta(id: Long?, organizacaoSolicitanteId: Long?): Voluntario {
        if (organizacaoSolicitanteId == null || organizacaoSolicitanteId <= 0) {
            throw ValidationException("Organização solicitante inválida")
        }
        return voluntarioAutorizado(id, organizacaoSolicitanteId)
    }

    @Transactional(readOnly = true)
    fun findAll(tokenData: TokenData): List<Voluntario> {
        val organizacaoId = organizacaoAutorizada(tokenData)
        return voluntarioOrganizacaoRepository.findAllByOrganizacaoId(organizacaoId)
            .filter { it.status == StatusVoluntario.ATIVO }
            .mapNotNull { it.voluntario }
            .distinctBy { it.id }
    }

    @Transactional(readOnly = true)
    fun findByCpf(cpf: String?, tokenData: TokenData): Voluntario {
        val organizacaoId = organizacaoAutorizada(tokenData)
        if (cpf.isNullOrBlank()) {
            throw ValidationException("CPF do voluntário é obrigatório")
        }

        val voluntario = voluntarioRepository.findByCpf(cpf)
            ?: throw ResourceNotFoundException("Voluntário não encontrado")
        return voluntarioAutorizado(voluntario.id, organizacaoId)
    }

    @Transactional
    fun update(id: Long, request: CreateUpdateVoluntario, tokenData: TokenData): Voluntario {
        val organizacaoId = organizacaoAutorizada(tokenData)
        val voluntario = voluntarioAutorizado(id, organizacaoId)

        // A entidade Voluntario é global. A edição alteraria os dados vistos
        // por qualquer outra organização com vínculo, então falha em segurança.
        val outrosVinculos = voluntarioOrganizacaoRepository
            .findAllByVoluntarioId(id)
            .any { it.organizacao?.id != organizacaoId }

        if (outrosVinculos) {
            throw AccessDeniedException(
                "Cadastro compartilhado requer autorização específica do titular"
            )
        }

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

    @Transactional
    fun desativarVinculo(voluntarioId: Long, tokenData: TokenData) {
        val organizacaoId = organizacaoAutorizada(tokenData)
        if (voluntarioId <= 0) {
            throw ValidationException("ID do voluntário inválido")
        }

        val vinculo = voluntarioOrganizacaoRepository
            .findByVoluntarioIdAndOrganizacaoId(voluntarioId, organizacaoId)
            ?: throw ResourceNotFoundException("Vínculo não encontrado")

        if (vinculo.status != StatusVoluntario.ATIVO) {
            throw ResourceNotFoundException("Vínculo ativo não encontrado")
        }

        voluntarioOrganizacaoRepository.save(vinculo.copy(status = StatusVoluntario.INATIVO))
    }
}
