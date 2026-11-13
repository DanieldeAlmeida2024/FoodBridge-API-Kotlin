"""Confere se todos os arquivos rastreados estao no inventario versionado."""
from pathlib import Path
import subprocess
import sys
import unittest

ROOT = Path(__file__).resolve().parents[2]


class InventoryContract(unittest.TestCase):
    def test_inventory_matches_git_tree(self):
        check = subprocess.run(
            [sys.executable, str(ROOT / "tools" / "atualizar_inventario.py"), "--check"],
            cwd=ROOT, capture_output=True, text=True, timeout=20,
        )
        self.assertEqual(check.returncode, 0, check.stdout + check.stderr)


if __name__ == "__main__":
    unittest.main()
