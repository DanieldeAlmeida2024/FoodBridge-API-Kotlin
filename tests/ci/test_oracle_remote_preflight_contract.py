"""Invariantes do job remoto independente: main, host fixado e nenhum deploy."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
workflow = (ROOT / ".github/workflows/oracle-preflight.yml").read_text(encoding="utf-8")

class OracleRemotePreflightContract(unittest.TestCase):
    def test_only_main_and_protected_environment(self):
        self.assertIn("github.ref == 'refs/heads/main'", workflow)
        self.assertIn("environment: oracle-production", workflow)
        self.assertIn("persist-credentials: false", workflow)

    def test_no_interactive_password_or_unverified_host(self):
        self.assertIn("-P ''", workflow)
        self.assertIn("StrictHostKeyChecking=yes", workflow)
        self.assertIn("BatchMode=yes", workflow)
        self.assertIn("IdentityAgent=none", workflow)
        self.assertIn("EXPECTED_RSA_FINGERPRINT", workflow)
        self.assertNotIn("ssh-keyscan", workflow)

    def test_remote_commands_only_authentication_and_readonly_script(self):
        self.assertIn("SSH_RSA_OK", workflow)
        self.assertIn("SSH_EXIT=%s", workflow)
        self.assertIn("'bash -s' < deploy/oracle/verificar-vm.sh", workflow)
        for forbidden in ("scp ", "atualizar-api.sh", "systemctl restart", "pg_restore", "psql ", "git pull"):
            self.assertNotIn(forbidden, workflow)

if __name__ == "__main__":
    unittest.main()
