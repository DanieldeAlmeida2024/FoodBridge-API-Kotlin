package com.br.foodbridge.config

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SchemaValidationPolicyTest {
    private fun verify(
        url: String? = "jdbc:postgresql://127.0.0.1:5432/foodbridge_restore_check_20261112",
        username: String? = "foodbridge_validate_20261112",
        ddlAuto: String? = "validate",
        web: String? = "none",
        readOnly: String? = "true",
        nativeDdl: String? = null
    ) = SchemaValidationPolicy.requireSafe(url, username, ddlAuto, web, readOnly, nativeDdl)

    @Test fun allowsRestoredCloneOnly() {
        assertDoesNotThrow { verify() }
        assertDoesNotThrow { verify(url = "jdbc:postgresql://localhost:5432/foodbridge_restore_check_case2") }
    }

    @Test fun rejectsOriginalAndRemoteDatabases() {
        listOf(
            "jdbc:postgresql://127.0.0.1:5432/foodbridge",
            "jdbc:postgresql://192.0.2.1:5432/foodbridge_restore_check_20261112",
            "jdbc:postgresql://127.0.0.1:5432/foodbridge_restore_check_20261112?currentSchema=public",
            "jdbc:postgresql://127.0.0.1:5432/postgres",
            "jdbc:postgresql://127.0.0.1:5433/foodbridge_restore_check_20261112"
        ).forEach { assertThrows(IllegalStateException::class.java) { verify(url = it) } }
    }

    @Test fun onlyDedicatedRoleMayConnect() {
        listOf("postgres", "foodbridge", "", "foodbridge_validate").forEach {
            assertThrows(IllegalStateException::class.java) { verify(username = it) }
        }
    }

    @Test fun noDDLMutationOrNativeOverride() {
        listOf("update", "create", "create-drop", "none", "").forEach {
            assertThrows(IllegalStateException::class.java) { verify(ddlAuto = it) }
        }
        assertThrows(IllegalStateException::class.java) { verify(nativeDdl = "update") }
    }

    @Test fun cannotStartHttpOrWriteEnabledPool() {
        assertThrows(IllegalStateException::class.java) { verify(web = "servlet") }
        assertThrows(IllegalStateException::class.java) { verify(readOnly = "false") }
    }
}
