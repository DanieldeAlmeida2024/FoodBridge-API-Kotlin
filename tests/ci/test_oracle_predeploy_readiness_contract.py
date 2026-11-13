"""Contrato read-only do predeploy direto via SSH na Oracle."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
WF = (ROOT / ".github/workflows/oracle-deploy-readiness.yml").read_text(encoding="utf-8")
SCRIPT = (ROOT / "deploy/oracle/verificar-predeploy-completo.sh").read_text(encoding="utf-8")

class OraclePreDeployContract(unittest.TestCase):
    def test_main_environment_ssh_pinned(self):
        for value in ("refs/heads/main", "environment: oracle-production",
                      "OCI_SSH_PRIVATE_KEY", "OCI_SSH_KNOWN_HOSTS",
                      "StrictHostKeyChecking=yes", "BatchMode=yes",
                      "IdentityAgent=none", "EXPECTED_RSA_FINGERPRINT"):
            if value == "EXPECTED_RSA_FINGERPRINT":
                self.assertIn("SHA256:3zV6jlZeYgt19xz4h0KrumznEr3SNlyAEiolj+XrmO8", WF)
            elif value in ("OCI_SSH_PRIVATE_KEY", "OCI_SSH_KNOWN_HOSTS"):
                self.assertIn(value, WF)
            else:
                self.assertIn(value, WF)

    def test_api_and_frontend_prerequisites(self):
        for value in ("API_GITHUB_DEPLOY_MARKER", "API_HTTP_HEALTH",
                      "API_OLD_JAR_PRESENT", "WEB_DIST_PRESENT", "WEB_PARENT_WRITABLE",
                      "WEB_NGINX_ACTIVE", "WEB_NGINX_CONFIG", "DEPLOY_READINESS=PASS"):
            self.assertIn(value, SCRIPT)
        self.assertIn("informativo, nao bloqueia o deploy FoodBridge", SCRIPT)

    def test_never_writes_or_restarts(self):
        for content in (WF, SCRIPT):
            for forbidden in ("systemctl restart", "systemctl stop",
                              "firewall-cmd --add", "chmod ", "chown ",
                              "docker restart", "pg_restore ", "sudo tee",
                              "git reset --hard", "scp ", "rsync ",
                              "atualizar-api.sh", "atualizar-frontend.sh"):
                self.assertNotIn(forbidden, content)

if __name__ == "__main__":
    unittest.main()
