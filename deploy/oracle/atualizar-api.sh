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
sudo -n test -f "$MARCADOR" || falhar "Execute antes deploy/oracle/preparar-vm.sh para configurar systemd."
sudo -n systemctl cat "$SERVICO" > /dev/null || falhar "Serviço foodbridge.service não encontrado."
sudo -n test -f "$DESTINO" || falhar "JAR anterior não encontrado; nenhuma implantação foi feita."

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
    curl --silent --show-error --fail --max-time 5 -o /dev/null \
      "http://127.0.0.1:8080/dashboards/publico"
}

echo "Reiniciando serviço $SERVICO (versão $IDENTIFICADOR)"
if sudo -n systemctl restart "$SERVICO"; then
  for tentativa in $(seq 1 30); do
    if saudavel; then
      echo "DEPLOY OK: API respondeu no endpoint público. Backup: $ANTIGO"
      exit 0
    fi
    sleep 2
  done
fi

echo "Falha de inicialização/health check. Restaurando o JAR anterior." >&2
NOVO="$DESTINO.rollback.$IDENTIFICADOR"
sudo -n install -m 0644 -- "$ANTIGO" "$NOVO"
sudo -n mv -f -- "$NOVO" "$DESTINO"
NOVO=""
sudo -n systemctl restart "$SERVICO" || true
if saudavel; then
  echo "Rollback executado e versão anterior voltou a responder." >&2
else
  echo "Rollback aplicado no disco, mas o serviço permanece indisponível. Verifique journalctl." >&2
fi
exit 1
