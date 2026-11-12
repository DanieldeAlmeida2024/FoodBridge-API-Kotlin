package com.br.foodbridge.config

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner

/**
 * Tests actual Spring @Profile wiring and the early BeanFactoryPostProcessor.
 * Does not load DataSource/JPA nor connect to PostgreSQL.
 */
class SchemaValidationContextTest {
    private fun runner() = ApplicationContextRunner()
        .withInitializer { context ->
            context.environment.setActiveProfiles("schema-validation")
        }
        .withUserConfiguration(DataInitializer::class.java, SchemaValidationGuard::class.java)
        .withPropertyValues(
            "spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/foodbridge_restore_check_20261112",
            "spring.datasource.username=foodbridge_validate_20261112",
            "spring.jpa.hibernate.ddl-auto=validate",
            "spring.main.web-application-type=none",
            "spring.datasource.hikari.read-only=true"
        )

    @Test
    fun validationProfileOmitsAdminInitializer() {
        runner().run { context ->
            assertNull(context.startupFailure)
            assertFalse(context.containsBean("dataInitializer"))
        }
    }

    @Test
    fun validationProfileRefusesOriginalDatabaseBeforeBeansAreReady() {
        runner()
            .withPropertyValues("spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/foodbridge")
            .run { context ->
                assertNotNull(context.startupFailure)
            }
    }
}
