package com.br.foodbridge.config

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import com.br.foodbridge.middleware.JwtFilter
import org.springframework.core.env.MapPropertySource

/**
 * Tests that Spring evaluates @Profile BEFORE registering the configuration.
 * No PostgreSQL JDBC, HTTP server, ORM, or admin seeding are started.
 */
class SchemaValidationContextTest {
    private fun createContext(url: String): AnnotationConfigApplicationContext {
        val context = AnnotationConfigApplicationContext()
        context.environment.setActiveProfiles("schema-validation")
        context.environment.propertySources.addFirst(
            MapPropertySource(
                "clone-only-test",
                mapOf<String, Any>(
                    "spring.datasource.url" to url,
                    "spring.datasource.username" to "foodbridge_validate_20261112",
                    "spring.jpa.hibernate.ddl-auto" to "validate",
                    "spring.main.web-application-type" to "none",
                    "spring.datasource.hikari.read-only" to "true"
                )
            )
        )
        context.register(
            DataInitializer::class.java,
            SchemaValidationGuard::class.java,
            JwtFilter::class.java,
            SecurityConfig::class.java,
            PasswordEncoderConfig::class.java
        )
        return context
    }

    @Test
    fun validationProfileOmitsAdminInitializer() {
        val context = createContext("jdbc:postgresql://127.0.0.1:5432/foodbridge_restore_check_20261112")
        try {
            context.refresh()
            assertFalse(context.containsBean("dataInitializer"))
            assertFalse(context.containsBean("init"))
            assertFalse(context.containsBean("jwtFilter"))
            assertFalse(context.containsBean("securityConfig"))
            assertTrue(context.containsBean("passwordEncoder"))
        } finally {
            context.close()
        }
    }

    @Test
    fun validationProfileRefusesProductionDatabaseAtBootstrap() {
        val context = createContext("jdbc:postgresql://127.0.0.1:5432/foodbridge")
        try {
            val failure = assertThrows(Exception::class.java) { context.refresh() }
            assertTrue(failure.stackTraceToString().contains("schema-validation: somente banco"))
        } finally {
            context.close()
        }
    }
}
