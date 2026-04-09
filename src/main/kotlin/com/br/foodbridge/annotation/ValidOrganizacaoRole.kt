package com.br.foodbridge.annotation

import com.br.foodbridge.domain.enums.OrganizacaoRole
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [OrganizacaoRoleValidator::class])
annotation class ValidOrganizacaoRole(
    val message: String = "Role inválido",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class OrganizacaoRoleValidator : ConstraintValidator<ValidOrganizacaoRole, OrganizacaoRole> {

    private val allowed = setOf(
        OrganizacaoRole.DOADOR,
        OrganizacaoRole.PRODUTOR,
        OrganizacaoRole.DISTRIBUIDOR,
        OrganizacaoRole.ONG
    )

    override fun isValid(value: OrganizacaoRole?, context: ConstraintValidatorContext): Boolean {
        return value != null && value in allowed
    }
}