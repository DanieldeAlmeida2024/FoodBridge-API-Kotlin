package com.br.foodbridge.config

import com.br.foodbridge.middleware.JwtFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtFilter: JwtFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    // Rotas públicas
                    .requestMatchers("/auth/login").permitAll()
                    .requestMatchers(HttpMethod.GET, "/dashboards/publico").permitAll()
                    .requestMatchers(HttpMethod.POST, "/usuarios").permitAll()
                    .requestMatchers("/error").permitAll()

                    // Rotas TEMP
                    .requestMatchers("/auth/select-org").hasAuthority("TOKEN_TEMP")
                    .requestMatchers(HttpMethod.POST, "/organizacoes").hasAuthority("TOKEN_TEMP")
                    .requestMatchers(HttpMethod.GET, "/organizacoes/cnpj/**").hasAuthority("TOKEN_TEMP")

                    // Rotas ADMIN
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET,"/organizacoes").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE,"/organizacoes/*").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET,"/usuarios").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE,"/usuarios/*").hasRole("ADMIN")

                    // Qualquer outra rota precisa de ACCESS token
                    .anyRequest().hasAuthority("TOKEN_ACCESS")
            }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
