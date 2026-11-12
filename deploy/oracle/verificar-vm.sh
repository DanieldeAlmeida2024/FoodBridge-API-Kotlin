#!/usr/bin/env bash
# FoodBridge Oracle Linux: auditoria SOMENTE LEITURA antes de qualquer deploy.
# Não imprime credenciais, não altera DB/JAR, não reinicia serviços.
set -Eeuo pipefail

SERVICE="foodbridge.service"
ROOT="/opt/foodbridge/FoodBridge-API-Kotlin"
JAR="$ROOT/build/libs/foodbridge-0.0.1-SNAPSHOT.jar"
CONFIG="/etc/foodbridge/application.properties"
DROPIN="/etc/systemd/system/foodbridge.service.d/90-github-deploy.conf"
HEALTH="http://127.0.0.1:8080/dashboards/publico"

fail() { printf 'PREFLIGHT FALHOU: %s\n' "$*" >&2; exit 1; }
check() { printf 'OK: %s\n' "$1"; }

sudo -n true >/dev/null 2>&1 || fail "sudo não interativo indisponível."
command -v sha256sum >/dev/null || fail "sha256sum indisponível."
command -v curl >/dev/null || fail "curl indisponível."
command -v systemctl >/dev/null || fail "systemctl indisponível."

sudo -n systemctl cat "$SERVICE" >/dev/null 2>&1 ||
  fail "foodbridge.service inexistente."
[[ "$(sudo -n systemctl show "$SERVICE" -p User --value)" == "opc" ]] ||
  fail "foodbridge.service não roda como opc."
check "systemd service e usuário esperados"

sudo -n test -s "$CONFIG" || fail "Config de produção externa ausente."
sudo -n -u opc test -r "$CONFIG" ||
  fail "O usuário do serviço não lê a configuração externa."
sudo -n test -f "$DROPIN" || fail "Drop-in 90-github-deploy.conf ausente."
sudo -n systemctl show "$SERVICE" -p Environment --value |
  grep -Fq 'SPRING_CONFIG_ADDITIONAL_LOCATION=file:/etc/foodbridge/' ||
  fail "Variável externa de configuração não foi aplicada no systemd."
check "configuração externa existente e legível (sem exibir valores)"


# Spring pode executar DDL durante restart; exigir política explicitamente não mutante.
sudo -n awk '
  /^[[:space:]]*[#!]/ { next }
  /^[[:space:]]*spring[.]jpa[.]hibernate[.]ddl-auto[[:space:]]*[=:]/ {
    count++
    value=$0
    sub(/^[^=:]*[=:][[:space:]]*/, "", value)
    sub(/[[:space:]]*[#!].*$/, "", value)
    sub(/[[:space:]]+$/, "", value)
    if (value != "validate" && value != "none") invalid=1
  }
  END { exit (count == 1 && !invalid) ? 0 : 1 }
' "$CONFIG" ||
  fail "Produção exige ddl-auto=validate (ou none) na configuração externa."
UNIT_ENV="$(sudo -n systemctl show "$SERVICE" -p Environment --value)"
if [[ "$UNIT_ENV" == *"SPRING_JPA_HIBERNATE_DDL_AUTO="* ]]; then
  [[ "$UNIT_ENV" =~ SPRING_JPA_HIBERNATE_DDL_AUTO=(validate|none)([[:space:]]|$) ]] ||
    fail "Override systemd de ddl-auto não é validate/none."
fi
unset UNIT_ENV
check "JPA ddl-auto explicitamente não mutante"

sudo -n test -s "$JAR" || fail "JAR esperado no destino não existe."
UNIT_EXEC="$(sudo -n systemctl show "$SERVICE" -p ExecStart --value)"
UNIT_DIR="$(sudo -n systemctl show "$SERVICE" -p WorkingDirectory --value)"
if [[ "$UNIT_EXEC" != *"$JAR"* ]]; then
  [[ "$UNIT_DIR" == "$ROOT" &&
     "$UNIT_EXEC" == *"build/libs/foodbridge-0.0.1-SNAPSHOT.jar"* ]] ||
    fail "ExecStart não aponta para o JAR que o pipeline substitui."
fi
unset UNIT_EXEC UNIT_DIR
check "JAR corresponde ao comando de inicialização efetivo"

sudo -n systemctl is-active --quiet "$SERVICE" ||
  fail "Serviço não está ativo antes do deploy."
curl --silent --show-error --fail --max-time 5 --output /dev/null "$HEALTH" ||
  fail "Endpoint público de saúde não respondeu antes do deploy."
check "serviço atual saudável via loopback"

printf 'PREFLIGHT ORACLE: PASS (somente leitura, nenhum deploy executado)\n'
