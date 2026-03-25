package com.br.foodbridge.config

import com.br.foodbridge.middleware.JwtFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
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
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/usuarios/criar").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET,"/organizacoes/").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE,"/organizacoes/{id}").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET,"/usuarios").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE,"/usuarios/{usuarioId}").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/voluntarios/**").authenticated()
                    .requestMatchers("/doacao/**").authenticated()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}