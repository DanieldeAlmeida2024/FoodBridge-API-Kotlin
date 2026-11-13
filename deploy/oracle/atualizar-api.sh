#!/usr/bin/env bash
# Atualiza somente o JAR da API FoodBridge e reverte automaticamente em caso de falha.
set -Eeuo pipefail

if [[ "$#" -ne 2 ]]; then
  echo "Uso: atualizar-api.sh <sha256> <arquivo-transferido>" >&2
  exit 2
fi

SHA_ESPERADO="$1"
UPLOAD="$2"
SERVICO="foodbridge.service"
RAIZ="/opt/foodbridge/FoodBridge-API-Kotlin"
DESTINO="$RAIZ/build/libs/foodbridge-0.0.1-SNAPSHOT.jar"
BACKUPS="$RAIZ/deploy-backups"
CONFIG="/etc/foodbridge/application.properties"
MARCADOR="/etc/systemd/system/foodbridge.service.d/90-github-deploy.conf"
NOVO=""
ANTIGO=""

limpar() {
  rm -f -- "$UPLOAD" 2>/dev/null || true
  if [[ -n "$NOVO" ]]; then sudo -n rm -f -- "$NOVO" || true; fi
}
trap limpar EXIT

falhar() { echo "ERRO: $*" >&2; exit 1; }
[[ "$SHA_ESPERADO" =~ ^[0-9a-f]{64}$ ]] || falhar "Checksum inválido."
[[ "$UPLOAD" == /tmp/foodbridge-deploy.*.jar ]] || falhar "Caminho temporário inesperado."
[[ -f "$UPLOAD" ]] || falhar "JAR recebido não encontrado."
echo "$SHA_ESPERADO  $UPLOAD" | sha256sum --check --status || falhar "SHA-256 diferente do pacote aprovado."
sudo -n true || falhar "Usuário remoto não tem sudo não interativo."
sudo -n test -f "$CONFIG" || falhar "Config externa de produção ausente: $CONFIG"
sudo -n -u opc test -r "$CONFIG" || falhar "O serviço (opc) não pode ler a configuração externa."
# Bloquear Hibernate DDL mutante no restart, antes de qualquer alteração de artefato.
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
  falhar "ddl-auto em produção deve ser validate ou none; DB intocado."
UNIT_ENV="$(sudo -n systemctl show "$SERVICO" -p Environment --value)"
if [[ "$UNIT_ENV" == *"SPRING_JPA_HIBERNATE_DDL_AUTO="* ]]; then
  [[ "$UNIT_ENV" =~ SPRING_JPA_HIBERNATE_DDL_AUTO=(validate|none)([[:space:]]|$) ]] ||
    falhar "Override systemd de ddl-auto deve ser validate ou none."
fi
unset UNIT_ENV
sudo -n test -f "$MARCADOR" || falhar "Execute antes deploy/oracle/preparar-vm.sh para configurar systemd."
sudo -n systemctl cat "$SERVICO" > /dev/null || falhar "Serviço foodbridge.service não encontrado."
sudo -n test -f "$DESTINO" || falhar "JAR anterior não encontrado; nenhuma implantação foi feita."
[[ "$(sudo -n systemctl show "$SERVICO" -p User --value)" == "opc" ]] ||
  falhar "O service systemd não executa como opc."
UNIT_EXEC="$(sudo -n systemctl show "$SERVICO" -p ExecStart --value)"
UNIT_DIR="$(sudo -n systemctl show "$SERVICO" -p WorkingDirectory --value)"
if [[ "$UNIT_EXEC" != *"$DESTINO"* ]]; then
  [[ "$UNIT_DIR" == "$RAIZ" &&
     "$UNIT_EXEC" == *"build/libs/foodbridge-0.0.1-SNAPSHOT.jar"* ]] ||
    falhar "ExecStart usa outro JAR; nenhum arquivo substituído."
fi
unset UNIT_EXEC UNIT_DIR
# Não substituir uma versão já indisponível: rollback não recuperaria saúde anterior.
sudo -n systemctl is-active --quiet "$SERVICO" ||
  falhar "Serviço não está ativo antes da tentativa de deploy."
curl --silent --show-error --fail --connect-timeout 2 --max-time 20 -o /dev/null \
  "http://127.0.0.1:8080/dashboards/publico" ||
  falhar "Health do serviço atual falhou; deploy cancelado antes da troca do JAR."

IDENTIFICADOR="$(date -u +%Y%m%dT%H%M%SZ)-${SHA_ESPERADO:0:12}"
ANTIGO="$BACKUPS/foodbridge-$IDENTIFICADOR.jar"
NOVO="$DESTINO.novo.$IDENTIFICADOR"

sudo -n install -d -m 0750 "$BACKUPS"
sudo -n cp -p -- "$DESTINO" "$ANTIGO"
sudo -n install -m 0644 -- "$UPLOAD" "$NOVO"
sudo -n mv -f -- "$NOVO" "$DESTINO"
NOVO=""

saudavel() {
  sudo -n systemctl is-active --quiet "$SERVICO" &&
    curl --silent --show-error --fail --connect-timeout 2 --max-time 20 -o /dev/null \
      "http://127.0.0.1:8080/dashboards/publico"
}

# Na Oracle com pouca RAM, o java pode iniciar somente depois de dezenas de segundos.
# Esperar de forma limitada TANTO no deploy quanto no rollback. Não declarar
# rollback indisponível ao primeiro "Connection refused" após systemctl restart.
aguardar_saude() {
  local inicio=$SECONDS
  local limite_segundos=180
  while (( SECONDS - inicio < limite_segundos )); do
    if saudavel; then
      return 0
    fi
    sleep 3
  done
  return 1
}

echo "Reiniciando serviço $SERVICO (versão $IDENTIFICADOR)"
if sudo -n systemctl restart "$SERVICO" && aguardar_saude; then
  echo "DEPLOY OK: API respondeu no endpoint público. Backup: $ANTIGO"
  exit 0
fi

echo "Falha de inicialização/health check. Restaurando o JAR anterior." >&2
NOVO="$DESTINO.rollback.$IDENTIFICADOR"
sudo -n install -m 0644 -- "$ANTIGO" "$NOVO"
sudo -n mv -f -- "$NOVO" "$DESTINO"
NOVO=""
if ! sudo -n systemctl restart "$SERVICO"; then
  echo "AVISO: systemctl restart do rollback retornou erro; verificando saúde mesmo assim." >&2
fi
if aguardar_saude; then
  echo "Rollback executado e versão anterior voltou a responder." >&2
else
  echo "Rollback aplicado no disco, mas serviço não respondeu dentro da janela de 180s. Verifique journalctl." >&2
fi
exit 1
