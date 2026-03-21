package com.br.foodbridge.config

import com.br.foodbridge.domain.enums.*
import com.br.foodbridge.domain.model.*
import com.br.foodbridge.domain.repository.OrganizacaoRepository
import com.br.foodbridge.domain.repository.UsuarioOrganizacaoRepository
import com.br.foodbridge.domain.repository.UsuarioRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class DataInitializer(
    private val usuarioRepository: UsuarioRepository,
    private val organizacaoRepository: OrganizacaoRepository,
    private val usuarioOrganizacaoRepository: UsuarioOrganizacaoRepository,
    private val passwordEncoder: PasswordEncoder,
    private val adminConfig: AdminConfig,
) {

    @Bean
    fun init() = CommandLineRunner {

        if (usuarioRepository.existsByEmail(adminConfig.email)) return@CommandLineRunner

        val organizacaoAdmin = organizacaoRepository.save(
            Organizacao(
                nome = "FoodBridge Admin",
                cnpj = "00000000000000",
                telefone = "000000000",
                role = OrganizacaoRole.ADMIN,
                email = adminConfig.email
            )
        )

        val admin = usuarioRepository.save(
            Usuario(
                nome = "Administrador",
                email = adminConfig.email,
                senha = passwordEncoder.encode(adminConfig.password)
            )
        )

        val vinculo = UsuarioOrganizacao(
            usuario = admin,
            organizacao = organizacaoAdmin,
            role = OrganizacaoRole.ADMIN,
            status = StatusOrganizacao.VERIFICADO
        )

        usuarioOrganizacaoRepository.save(vinculo)
    }
}