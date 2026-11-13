#!/usr/bin/env python3
"""Atualiza o bloco gerado em docs/INVENTARIO_PROJETO.md a partir do Git.

Uso:
  python3 tools/atualizar_inventario.py --write
  python3 tools/atualizar_inventario.py --check

O arquivo segue versionado; a validacao do CI detecta caminhos obsoletos.
Nao escreve fora de docs/INVENTARIO_PROJETO.md, nao acessa banco nem producao.
"""
from __future__ import annotations

import argparse
import subprocess
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DOC = ROOT / "docs" / "INVENTARIO_PROJETO.md"
START = "<!-- INVENTARIO_ARQUIVOS_INICIO -->"
END = "<!-- INVENTARIO_ARQUIVOS_FIM -->"


def tracked_files() -> list[str]:
    output = subprocess.check_output(["git", "ls-files", "-z"], cwd=ROOT)
    return sorted(p.decode("utf-8") for p in output.split(b"\0") if p)


def generated_section(paths: list[str]) -> str:
    sections = Counter(p.split("/", 1)[0] if "/" in p else "(raiz)" for p in paths)
    text = [f"**Total de arquivos rastreados:** {len(paths)}.", "",
            "**Distribuicao por pasta de primeiro nivel:**"]
    for name, count in sorted(sections.items()):
        text.append(f"- `{name}`: {count}")
    text.extend(["", "**Lista completa de caminhos versionados (gerada do Git):**", ""])
    text.extend(f"- `{path}`" for path in paths)
    return "\n".join(text) + "\n"


def main() -> int:
    parser = argparse.ArgumentParser(description="Reconciliar inventario de arquivos com git ls-files")
    operation = parser.add_mutually_exclusive_group(required=True)
    operation.add_argument("--write", action="store_true")
    operation.add_argument("--check", action="store_true")
    args = parser.parse_args()
    if not DOC.exists():
        print(f"Inventario ausente: {DOC.relative_to(ROOT)}", file=sys.stderr)
        return 1
    original = DOC.read_text(encoding="utf-8")
    if original.count(START) != 1 or original.count(END) != 1:
        print("Marcadores unicos ausentes no inventario.", file=sys.stderr)
        return 1
    prefix, remainder = original.split(START, 1)
    _, suffix = remainder.split(END, 1)
    updated = prefix + START + "\n" + generated_section(tracked_files()) + END + suffix
    if args.check:
        if original != updated:
            print("Inventario desatualizado: rode python3 tools/atualizar_inventario.py --write", file=sys.stderr)
            return 1
        print("INVENTARIO_GIT=PASS")
        return 0
    if updated != original:
        DOC.write_text(updated, encoding="utf-8")
    print("INVENTARIO_ATUALIZADO=YES")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
