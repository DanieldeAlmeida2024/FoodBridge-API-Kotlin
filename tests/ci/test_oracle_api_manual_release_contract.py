"""Contrato da operação manual de primeiro deploy API via Windows/GH CLI."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
PS = (ROOT / "deploy/oracle/publicar-api-manual-windows.ps1").read_text(encoding="utf-8")

class ManualAPIDeployGate(unittest.TestCase):
    def test_requires_explicit_manual_action(self):
        self.assertIn("[switch]$Publicar", PS)
        self.assertIn("if (-not $Publicar)", PS)
        self.assertIn("gh.exe auth status", PS)

    def test_only_dispatch_not_auto_deploy(self):
        self.assertIn("OCI_AUTO_DEPLOY_ENABLED --body 'false'", PS)
        self.assertIn("OCI_DEPLOY_ENABLED --body 'true'", PS)
        self.assertIn("workflow run foodbridge-ci-cd.yml --ref main -f preflight=false -f deploy=true", PS)
        self.assertNotIn("OCI_AUTO_DEPLOY_ENABLED --body 'true'", PS)

    def test_waits_for_real_job_and_external_http(self):
        for text in ("actions/runs/([0-9]+)", "run watch $runId", "--exit-status",
                     "run view $runId", "--json jobs", "Publicar na VM Oracle",
                     ".conclusion -ne 'success'", "dashboards/publico",
                     "API_EXTERNAL_HTTP=200"):
            self.assertIn(text, PS)
        self.assertNotIn("gh.exe run list", PS)

    def test_restores_manual_flag_and_does_not_handle_secrets(self):
        self.assertIn("finally {", PS)
        self.assertIn("OCI_DEPLOY_ENABLED --body 'false'", PS)
        self.assertNotIn("OCI_SSH_PRIVATE_KEY", PS)
        self.assertNotIn("postgresql", PS.lower())
        self.assertNotIn("systemctl restart", PS)
        self.assertNotIn("Tailscale", PS)

if __name__ == "__main__":
    unittest.main()
