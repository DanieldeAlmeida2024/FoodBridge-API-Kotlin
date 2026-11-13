"""Regressao: nao substituir JAR se a VM estiver subdimensionada."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
GATE = (ROOT / "deploy/oracle/verificar-recursos-deploy.sh").read_text(encoding="utf-8")
WF = (ROOT / ".github/workflows/foodbridge-ci-cd.yml").read_text(encoding="utf-8")

class MemoryGateContract(unittest.TestCase):
    def test_gate_is_readonly_and_conservative(self):
        for phrase in ("MemTotal:", "MemAvailable:", "MIN_TOTAL_KIB=1048576",
                       "MIN_AVAILABLE_KIB=262144", "DEPLOY_CAPACITY=BLOCKED",
                       "DEPLOY_CAPACITY=PASS"):
            self.assertIn(phrase, GATE)
        for command in ("systemctl restart", "sudo tee", "swapon ", "swapoff ",
                        "pg_restore", "rm -rf", "chmod ", "chown ", "mv ", "cp "):
            self.assertNotIn(command, GATE)

    def test_gate_is_before_transfer_restart(self):
        self.assertIn("bash -n deploy/oracle/verificar-recursos-deploy.sh", WF)
        preflight = WF.index("      - name: Pré-flight obrigatório sem escrita antes do deploy")
        gate = WF.index("      - name: Gate de recursos para release - somente leitura")
        transfer = WF.index("      - name: Transferir JAR e aplicar com rollback")
        self.assertLess(preflight, gate)
        self.assertLess(gate, transfer)
        self.assertIn("ssh \"${SSH_OPTS[@]}\"", WF[gate:transfer])

if __name__ == "__main__":
    unittest.main()
