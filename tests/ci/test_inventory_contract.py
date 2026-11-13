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


    def test_documentation_excludes_unrelated_host_applications(self):
        """Nao transformar outros sistemas do host em partes do FoodBridge."""
        forbidden = ("animedex", "scraper", "puppeteer", "chromium", "8787")
        checked = [ROOT / "README.md", ROOT / "AGENTS.md"]
        checked += list((ROOT / "docs").rglob("*.md"))
        checked += list((ROOT / "docs").rglob("*.puml"))
        for path in checked:
            content = path.read_text(encoding="utf-8").casefold()
            for term in forbidden:
                self.assertNotIn(term, content, f"{path.relative_to(ROOT)}: referencias fora do escopo FoodBridge")


if __name__ == "__main__":
    unittest.main()
