package com.br.foodbridge.config

import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.context.annotation.Profile
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

/**
 * Validate a restored database without booting the HTTP server or admin data seeder.
 * Credential privileges must ALSO enforce PostgreSQL read-only access.
 */
@Component
@Profile("schema-validation")
class SchemaValidationGuard : BeanFactoryPostProcessor, EnvironmentAware {
    private lateinit var environment: Environment

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        SchemaValidationPolicy.requireSafe(
            datasourceUrl = environment.getProperty("spring.datasource.url"),
            username = environment.getProperty("spring.datasource.username"),
            ddlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto"),
            webApplicationType = environment.getProperty("spring.main.web-application-type"),
            hikariReadOnly = environment.getProperty("spring.datasource.hikari.read-only"),
            nativeDdlOverride = environment.getProperty("spring.jpa.properties.hibernate.hbm2ddl.auto")
        )
    }
}

internal object SchemaValidationPolicy {
    private val cloneUrl = Regex(
        "^jdbc:postgresql://(?:127\\.0\\.0\\.1|localhost):5432/foodbridge_restore_check_[A-Za-z0-9_]+$"
    )
    private val readOnlyUser = Regex("^foodbridge_validate_[A-Za-z0-9_]+$")

    fun requireSafe(
        datasourceUrl: String?,
        username: String?,
        ddlAuto: String?,
        webApplicationType: String?,
        hikariReadOnly: String?,
        nativeDdlOverride: String?
    ) {
        check(datasourceUrl != null && cloneUrl.matches(datasourceUrl)) {
            "schema-validation: somente banco foodbridge_restore_check_* no loopback."
        }
        check(username != null && readOnlyUser.matches(username)) {
            "schema-validation: exige role PostgreSQL dedicada foodbridge_validate_*."
        }
        check(ddlAuto == "validate" && nativeDdlOverride.isNullOrBlank()) {
            "schema-validation: exige ddl-auto=validate, sem override nativo."
        }
        check(webApplicationType.equals("none", ignoreCase = true)) {
            "schema-validation: exige web-application-type=none."
        }
        check(hikariReadOnly.equals("true", ignoreCase = true)) {
            "schema-validation: exige hikari.read-only=true e role DB somente leitura."
        }
    }
}
