"""Garante que o diagnostico de rede da Oracle nao modifica a VM."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
WORKFLOW = (ROOT / ".github/workflows/oracle-network-diagnostics.yml").read_text(encoding="utf-8")

class OracleNetworkDiagnosticsContract(unittest.TestCase):
    def test_only_main_with_environment_and_key_pinned(self):
        for fragment in ("github.ref == 'refs/heads/main'", "environment: oracle-production",
                         "StrictHostKeyChecking=yes", "IdentityAgent=none",
                         "OCI_SSH_PRIVATE_KEY", "OCI_SSH_KNOWN_HOSTS",
                         "3zV6jlZeYgt19xz4h0KrumznEr3SNlyAEiolj+XrmO8"):
            self.assertIn(fragment, WORKFLOW)

    def test_public_and_private_checks_cover_worker_and_nginx(self):
        for fragment in ("EXTERNAL_PORT_", "LOCAL_PORT_", "NGINX_CONFIG",
                         "DOCKER_ANIMEDEX", "FIREWALLD_ACTIVE_ZONE_RULES",
                         "8787|/health", "80|/", "DIAGNOSTIC_DONE=YES"):
            self.assertIn(fragment, WORKFLOW)

    def test_remote_inspection_is_readonly(self):
        remote = WORKFLOW.split("<<'REMOTE'", 1)[1].split("          REMOTE", 1)[0]
        for forbidden in ("systemctl restart", "systemctl stop", "systemctl start",
                          "firewall-cmd --add", "firewall-cmd --remove",
                          "nginx -s reload", "docker restart", "docker rm",
                          "docker run", "iptables -A", "nft add", "pg_restore",
                          "sudo tee", "git reset", "git pull"):
            self.assertNotIn(forbidden, remote)

if __name__ == "__main__":
    unittest.main()
