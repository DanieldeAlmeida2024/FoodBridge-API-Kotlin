"""Restringe diagnostico do incidente API a leitura de estado, sem alterar a Oracle."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
SCRIPT = (ROOT / "deploy/oracle/diagnosticar-falha-startup.sh").read_text(encoding="utf-8")
WORKFLOW = (ROOT / ".github/workflows/oracle-api-incident.yml").read_text(encoding="utf-8")

class OracleAPIIncidentContract(unittest.TestCase):
    def test_remote_script_does_not_mutate_host(self):
        for forbidden in ("systemctl restart", "systemctl stop", "systemctl start",
                          "docker restart", "sudo tee", "firewall-cmd --add",
                          "pg_restore", "psql -c", "rm -rf", "chmod ", "chown ",
                          "git pull", "scp ", "atualizar-api.sh"):
            self.assertNotIn(forbidden, SCRIPT)

    def test_reads_service_ports_and_rollbacks(self):
        for expected in ("foodbridge.service", "journalctl -u foodbridge.service",
                         "EXCEPTION_CLASS_NAMES", "JOURNAL_ERRORS_BY_CATEGORY",
                         "SQLSTATE_CODES", "CURRENT_JAR_SHA256",
                         "RECENT_BACKUP", "HTTP_8080", "HTTP_80",
                         "POSTGRES_TCP_READY_EXIT", "INCIDENT_DIAGNOSTIC_FINISHED"):
            if expected in ("HTTP_8080", "HTTP_80"):
                self.assertIn("HTTP_", SCRIPT)
            else:
                self.assertIn(expected, SCRIPT)
        self.assertIn("CRASHKERNEL_ARG_PRESENT", SCRIPT)
        self.assertIn("KDUMP_SERVICE_STATUS", SCRIPT)
        self.assertIn("grep -Eq", SCRIPT)
        for expected in ("MEMORY_PRESSURE_AND_SWAP_ACTIVITY", "MEMORY_PSI_", "VMSTAT_INTERVALS", "VMSTAT_SWAP_ACTIVE_INTERVALS", "VMSTAT_MAX_SWAPIN_KIBPS", "VMSTAT_MAX_SWAPOUT_KIBPS", "VMSTAT_MIN_CPU_IDLE_PCT", "VMSTAT_MAX_IOWAIT_PCT", "DOCKER_CONTAINER="):
            self.assertIn(expected, SCRIPT)
        self.assertIn("vmstat 1 6", SCRIPT)
        self.assertIn("NR>=4", SCRIPT)  # Exclui media desde o boot
        self.assertNotIn("journalctl -u foodbridge.service --no-pager -n 1200 | tee", SCRIPT)

    def test_pinned_ssh_and_only_main_without_deploy(self):
        for expected in ("environment: oracle-production", "refs/heads/main",
                         "StrictHostKeyChecking=yes", "BatchMode=yes",
                         "IdentityAgent=none", "OCI_SSH_PRIVATE_KEY",
                         "OCI_SSH_KNOWN_HOSTS", "diagnosticar-falha-startup.sh"):
            self.assertIn(expected, WORKFLOW)
        self.assertNotIn("Publicar na VM Oracle", WORKFLOW)
        self.assertNotIn("OCI_DEPLOY_ENABLED", WORKFLOW)

if __name__ == "__main__":
    unittest.main()
