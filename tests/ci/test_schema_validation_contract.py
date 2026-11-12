"""Protect one-off clone-only schema validation configuration from regressions."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
init = (ROOT / "src/main/kotlin/com/br/foodbridge/config/DataInitializer.kt").read_text()
guard = (ROOT / "src/main/kotlin/com/br/foodbridge/config/SchemaValidationGuard.kt").read_text()

class SchemaValidationContract(unittest.TestCase):
    def test_mvc_security_excluded_but_password_encoder_kept(self):
        jwt = (ROOT / "src/main/kotlin/com/br/foodbridge/middleware/JwtFilter.kt").read_text()
        security = (ROOT / "src/main/kotlin/com/br/foodbridge/config/SecurityConfig.kt").read_text()
        encoder = (ROOT / "src/main/kotlin/com/br/foodbridge/config/PasswordEncoderConfig.kt").read_text()
        self.assertIn('@Profile("!schema-validation")', jwt)
        self.assertIn('@Profile("!schema-validation")', security)
        self.assertIn("BCryptPasswordEncoder()", encoder)
        self.assertIn("@Bean", encoder)
        self.assertNotIn("fun passwordEncoder()", security)

    def test_disable_admin_initializer_in_validation_profile(self):
        self.assertIn('@Profile("!schema-validation")', init)
        self.assertIn('@Profile("schema-validation")', guard)

    def test_guard_is_explicit_and_non_prod(self):
        for phrase in ("foodbridge_restore_check_", "foodbridge_validate_", 'ddlAuto == "validate"',
                       'webApplicationType.equals("none"', 'hikariReadOnly.equals("true"'):
            self.assertIn(phrase, guard)

if __name__ == "__main__":
    unittest.main()
