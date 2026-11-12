"""Execução do preflight Oracle em ambiente inteiramente simulado, sem SSH/DB."""
import os
from pathlib import Path
import subprocess
import tempfile
import textwrap
import unittest

ROOT = Path(__file__).resolve().parents[2]
SCRIPT = ROOT / "deploy/oracle/verificar-vm.sh"

FAKE_SUDO = r'''#!/usr/bin/env python3
import os, sys
args=sys.argv[1:]
if args[:1] == ["-n"]:
    args=args[1:]
if args[:2] == ["-u","opc"]:
    args=args[2:]
if not args:
    sys.exit(1)
if args[0] == "true":
    sys.exit(0)
if args[0] == "test":
    if os.environ.get("SIM_MISSING_CONFIG") == "1" and args[-1] == "/etc/foodbridge/application.properties":
        sys.exit(1)
    sys.exit(0)
if args[0] == "grep":
    sys.exit(0 if os.environ.get("SIM_DDL", "validate") in ("validate", "none") else 1)
if args[0] == "systemctl":
    if "cat" in args:
        sys.exit(0)
    if "is-active" in args:
        sys.exit(1 if os.environ.get("SIM_HEALTH") == "down" else 0)
    if "show" in args:
        if "User" in args:
            print("root" if os.environ.get("SIM_WRONG_USER") == "1" else "opc")
        elif "Environment" in args:
            token="SPRING_CONFIG_ADDITIONAL_LOCATION=file:/etc/foodbridge/"
            if os.environ.get("SIM_BAD_OVERRIDE") == "1":
                token+=" SPRING_JPA_HIBERNATE_DDL_AUTO=update"
            print(token)
        elif "ExecStart" in args:
            if os.environ.get("SIM_WRONG_JAR") == "1":
                print("/usr/bin/java -jar /opt/other/foodbridge.jar")
            else:
                print("/usr/bin/java -jar /opt/foodbridge/FoodBridge-API-Kotlin/build/libs/foodbridge-0.0.1-SNAPSHOT.jar")
        elif "WorkingDirectory" in args:
            print("/opt/foodbridge/FoodBridge-API-Kotlin")
        else:
            sys.exit(1)
        sys.exit(0)
sys.exit(1)
'''

FAKE_CURL = r'''#!/usr/bin/env python3
import os, sys
sys.exit(1 if os.environ.get("SIM_HEALTH") == "down" else 0)
'''


class OraclePreflightMockTests(unittest.TestCase):
    def run_preflight(self, **overrides):
        with tempfile.TemporaryDirectory(prefix="foodbridge-preflight-") as dirname:
            directory=Path(dirname)
            for name, source in [
                ("sudo",FAKE_SUDO), ("curl",FAKE_CURL),
                ("systemctl","#!/usr/bin/env sh\nexit 0\n"),
            ]:
                file=directory/name
                file.write_text(source,encoding="utf-8")
                file.chmod(0o700)
            env=os.environ.copy()
            env["PATH"]=str(directory)+os.pathsep+env.get("PATH","")
            env.update(overrides)
            return subprocess.run(
                ["bash", str(SCRIPT)], cwd=ROOT, env=env,
                capture_output=True, text=True, timeout=7, check=False
            )

    def test_healthy_host_without_any_write_passes(self):
        result=self.run_preflight()
        self.assertEqual(result.returncode,0,result.stderr)
        self.assertIn("PREFLIGHT ORACLE: PASS",result.stdout)

    def test_rejects_unsafe_hibernate_ddl(self):
        result=self.run_preflight(SIM_DDL="update")
        self.assertNotEqual(result.returncode,0)
        self.assertIn("ddl-auto",result.stderr)

    def test_rejects_environment_override_of_ddl(self):
        result=self.run_preflight(SIM_BAD_OVERRIDE="1")
        self.assertNotEqual(result.returncode,0)
        self.assertIn("ddl-auto",result.stderr)

    def test_rejects_different_running_jar(self):
        result=self.run_preflight(SIM_WRONG_JAR="1")
        self.assertNotEqual(result.returncode,0)
        self.assertIn("ExecStart",result.stderr)

    def test_rejects_unhealthy_service(self):
        result=self.run_preflight(SIM_HEALTH="down")
        self.assertNotEqual(result.returncode,0)
        self.assertIn("Serviço",result.stderr)

    def test_rejects_wrong_service_user(self):
        result=self.run_preflight(SIM_WRONG_USER="1")
        self.assertNotEqual(result.returncode,0)
        self.assertIn("opc",result.stderr)

    def test_rejects_missing_external_config(self):
        result=self.run_preflight(SIM_MISSING_CONFIG="1")
        self.assertNotEqual(result.returncode,0)
        self.assertIn("Config",result.stderr)


if __name__ == "__main__":
    unittest.main()
