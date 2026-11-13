"""Guardas locais do contrato GitHub Actions -> Oracle Cloud (sem SSH)."""
import re
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
wf = (ROOT / ".github/workflows/foodbridge-ci-cd.yml").read_text(encoding="utf-8")
preflight = (ROOT / "deploy/oracle/verificar-vm.sh").read_text(encoding="utf-8")
deploy = (ROOT / "deploy/oracle/atualizar-api.sh").read_text(encoding="utf-8")
prepare = (ROOT / "deploy/oracle/preparar-vm.sh").read_text(encoding="utf-8")


class OracleDeployContract(unittest.TestCase):
    def test_ci_isolated_and_deploy_opt_in(self):
        self.assertIn("postgres:16-alpine", wf)
        self.assertIn("clean test bootJar", wf)
        self.assertIn("concurrency:", wf)
        self.assertIn("cancel-in-progress: false", wf)
        self.assertIn("OCI_DEPLOY_ENABLED == 'true'", wf)
        deploy_job = wf.split("  deploy-oracle:", 1)[1]
        self.assertIn("github.event_name == 'workflow_dispatch' && inputs.deploy == true", deploy_job)
        self.assertIn("github.event_name == 'push' && vars.OCI_AUTO_DEPLOY_ENABLED == 'true'", deploy_job)
        self.assertIn("vars.OCI_DEPLOY_ENABLED == 'true'", deploy_job)
        self.assertIn("github.ref == 'refs/heads/main'", deploy_job)
        self.assertIn("needs: [build-e-testes]", deploy_job)
        self.assertIn("!inputs.preflight", deploy_job)
        self.assertNotIn("github.event_name == 'pull_request'", deploy_job)

    def test_preflight_is_manual_and_read_only(self):
        self.assertIn("verificar-oracle:", wf)
        self.assertIn("github.event_name == 'workflow_dispatch' && inputs.preflight == true", wf)
        self.assertIn("'bash -s' < deploy/oracle/verificar-vm.sh", wf)
        for forbidden in [
            r"\b(?:systemctl\s+(?:restart|start|stop))\b",
            r"\b(?:psql|pg_restore|pg_dump|scp)\b",
            r"\b(?:rm|mv|cp|install|tee|git\s+pull|git\s+reset)\b",
        ]:
            self.assertIsNone(re.search(forbidden, preflight), "preflight contains writes: " + forbidden)

    def test_host_verification_and_key_never_exposed_in_commands(self):
        self.assertIn("StrictHostKeyChecking=yes", wf)
        self.assertIn("IdentitiesOnly=yes", wf)
        self.assertIn("OCI_SSH_KNOWN_HOSTS", wf)
        self.assertIn("ssh-keygen -y -f", wf)
        self.assertNotIn("ssh-keyscan ", wf, "runner must not trust SSH host key on first use")
        self.assertNotIn("cat /etc/foodbridge/application.properties", preflight)
        self.assertNotIn("cat /etc/foodbridge/application.properties", deploy)

    def test_preflight_matches_deployed_jar_and_validates_health(self):
        for script in (preflight, deploy, prepare):
            self.assertIn("systemctl show", script)
            self.assertIn("-p ExecStart --value", script)
            self.assertIn("build/libs/foodbridge-0.0.1-SNAPSHOT.jar", script)
        self.assertIn("PREFLIGHT ORACLE: PASS", preflight)
        self.assertIn("systemctl is-active", preflight)
        self.assertIn("dashboards/publico", preflight)
        self.assertIn("SPRING_CONFIG_ADDITIONAL_LOCATION=file:/etc/foodbridge/", preflight)

    def test_slow_database_dashboard_does_not_cause_false_rollback(self):
        # Measured successful HTTP 200 in VM ~6.25s; previous 5s cutoff was incorrect.
        self.assertIn("--max-time 20", preflight)
        self.assertEqual(deploy.count("--max-time 20"), 2)
        self.assertNotIn("--max-time 5", preflight + deploy)
        self.assertIn("--connect-timeout 2", preflight)
        self.assertIn("AVISO: dashboard público lento", preflight)

    def test_rollback_waits_for_spring_to_be_healthy(self):
        # No incidente 13/11, rollback do JAR estava correto, mas o JVM levou
        # tempo ate disponibilizar a porta 8080. Verificar janela nas duas fases.
        self.assertIn("aguardar_saude() {", deploy)
        self.assertIn("local limite_segundos=180", deploy)
        self.assertIn("SECONDS - inicio < limite_segundos", deploy)
        self.assertIn('if sudo -n systemctl restart "$SERVICO" && aguardar_saude; then', deploy)
        self.assertIn("if aguardar_saude; then", deploy)
        self.assertEqual(deploy.count("aguardar_saude; then"), 2)
        self.assertNotIn('if saudavel; then\n  echo "Rollback executado', deploy)
    def test_integrity_backup_rollback_and_no_git_db_reset(self):
        self.assertIn("sha256sum --check --status", deploy)
        self.assertIn('sudo -n cp -p -- "$DESTINO" "$ANTIGO"', deploy)
        self.assertIn("Rollback aplicado no disco", deploy)
        self.assertIn("systemctl restart", deploy)
        self.assertIn("systemctl is-active", deploy)
        self.assertNotIn("git reset --hard", deploy + prepare)
        self.assertNotIn("psql ", deploy + prepare)
        self.assertNotIn("pg_restore", deploy + prepare)

    def test_ddl_auto_remains_non_mutating_on_service_restart(self):
        for script in (preflight, deploy):
            self.assertIn("spring[.]jpa[.]hibernate[.]ddl-auto", script)
            self.assertIn("(validate|none)", script)
            self.assertIn("SPRING_JPA_HIBERNATE_DDL_AUTO", script)
            self.assertNotIn("ddl-auto=update", script.replace("# O restart do Spring pode executar DDL se spring.jpa.hibernate.ddl-auto=update.", ""))


if __name__ == "__main__":
    unittest.main()
