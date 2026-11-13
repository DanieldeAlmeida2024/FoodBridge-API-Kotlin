"""Contratos de seguranca do agente outbound de publicacao da Oracle."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
SCRIPT = (ROOT / "deploy/oracle/pull-approved-artifact.sh").read_text(encoding="utf-8")
WORKFLOW = (ROOT / ".github/workflows/foodbridge-ci-cd.yml").read_text(encoding="utf-8")
SERVICE = (ROOT / "deploy/oracle/systemd/foodbridge-pull@.service").read_text(encoding="utf-8")

class OutboundDeployContractTest(unittest.TestCase):
    def test_only_approved_main_push_artifact(self):
        for text in ("--branch main", "--event push", "--status success", "headSha", "databaseId",
                     "gh run download", "commits/main", "foodbridge-api-jar", "foodbridge-frontend-dist"):
            self.assertIn(text, SCRIPT)

    def test_fail_closed_until_enabled_and_check_mode(self):
        for text in ("--check", "--apply", "FOODBRIDGE_PULL_DEPLOY_ENABLED:-false",
                     "CHECK PASS", "DEPLOY_DESATIVADO", "MAIN"):
            if text == "MAIN":
                self.assertIn("Main mudou", SCRIPT)
            else:
                self.assertIn(text, SCRIPT)
        self.assertNotIn("git pull", SCRIPT)
        self.assertNotIn("ssh ", SCRIPT)
        self.assertNotIn("scp ", SCRIPT)

    def test_preserve_existing_deploy_and_backup(self):
        self.assertIn("bash \"$HERE/verificar-vm.sh\"", SCRIPT)
        self.assertIn("bash \"$HERE/atualizar-api.sh\"", SCRIPT)
        self.assertIn("nginx -t", SCRIPT)
        self.assertIn("BACKUP=", SCRIPT)
        self.assertIn("Rollback frontend aplicado", SCRIPT)
        self.assertIn("test -s \"$TMP/package/index.html\"", SCRIPT)

    def test_systemd_is_separate_from_application(self):
        self.assertIn("Type=oneshot", SERVICE)
        self.assertIn("User=opc", SERVICE)
        self.assertIn("pull-deploy.conf", SERVICE)
        self.assertIn("%i --apply", SERVICE)
        self.assertIn("bash -n deploy/oracle/pull-approved-artifact.sh", WORKFLOW)

if __name__ == "__main__":
    unittest.main()
