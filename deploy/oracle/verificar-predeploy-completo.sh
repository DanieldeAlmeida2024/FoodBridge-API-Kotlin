#!/usr/bin/env bash
# Auditoria estritamente somente leitura dos requisitos para o primeiro deploy SSH.
# Utilizar em sessao SSH com usuario opc; nao publica, reinicia ou altera dados.
set -uo pipefail

failures=0
check() {
  label="$1"
  shift
  if "$@" >/dev/null 2>&1; then
    printf '%s=PASS\n' "$label"
  else
    printf '%s=FAIL\n' "$label"
    failures=$((failures + 1))
  fi
}
check_value() {
  label="$1"
  expected="$2"
  actual="$3"
  if [[ "$actual" == "$expected" ]]; then
    echo "$label=PASS"
  else
    echo "$label=FAIL"
    failures=$((failures + 1))
  fi
}

API_ROOT=/opt/foodbridge/FoodBridge-API-Kotlin
WEB_ROOT=/opt/foodbridge/foodbridge-frontend/foodbridge-frontend

echo '==== API_DEPLOY_READINESS ===='
check API_SUDO_NONINTERACTIVE sudo -n true
check API_SYSTEMD_ACTIVE sudo -n systemctl is-active --quiet foodbridge.service
check API_EXTERNAL_CONFIG_PRESENT sudo -n test -s /etc/foodbridge/application.properties
check API_GITHUB_DEPLOY_MARKER sudo -n test -f /etc/systemd/system/foodbridge.service.d/90-github-deploy.conf
check API_OLD_JAR_PRESENT sudo -n test -s "$API_ROOT/build/libs/foodbridge-0.0.1-SNAPSHOT.jar"
check API_HTTP_HEALTH curl -sf --connect-timeout 2 --max-time 20 -o /dev/null http://127.0.0.1:8080/dashboards/publico
user="$(sudo -n systemctl show foodbridge.service -p User --value 2>/dev/null || true)"
check_value API_SERVICE_USER opc "$user"

echo '==== FRONTEND_DEPLOY_READINESS ===='
check WEB_NGINX_ACTIVE sudo -n systemctl is-active --quiet nginx
check WEB_NGINX_CONFIG sudo -n nginx -t
check WEB_DIST_PRESENT test -s "$WEB_ROOT/dist/index.html"
check WEB_PARENT_WRITABLE test -w "$WEB_ROOT"
check WEB_TOOLS_COMMANDS bash -c 'command -v tar >/dev/null && command -v python3 >/dev/null && command -v sha256sum >/dev/null && command -v restorecon >/dev/null'
check WEB_RESTORECON_SUDO sudo -n true

echo '==== OTHER_APPS_READ_ONLY ===='
check ANIMEDEX_WORKER_HEALTH curl -sf --connect-timeout 2 --max-time 8 -o /dev/null http://127.0.0.1:8787/health

echo "DEPLOY_READINESS_FAILURES=$failures"
if (( failures > 0 )); then
  echo 'DEPLOY_READINESS=BLOCKED (sem escrita, nenhuma publicacao executada)'
  exit 1
fi
echo 'DEPLOY_READINESS=PASS (somente leitura; nao comprova o primeiro deploy real)'
