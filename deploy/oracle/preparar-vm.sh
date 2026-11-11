#!/usr/bin/env bash
# Executar UMA VEZ na VM Oracle, conectado como opc, após conferir o arquivo externo.
set -Eeuo pipefail

ROOT="/opt/foodbridge/FoodBridge-API-Kotlin"
ORIGEM="$ROOT/src/main/resources/application.properties"
CONFIG="/etc/foodbridge/application.properties"
OVERRIDE="/etc/systemd/system/foodbridge.service.d/90-github-deploy.conf"

sudo -n true || { echo "Execute como usuário com sudo não interativo (opc)." >&2; exit 1; }
sudo -n systemctl cat foodbridge.service > /dev/null || {
  echo "Serviço foodbridge.service não encontrado; confirme o deploy existente." >&2
  exit 1
}

if ! sudo -n test -s "$CONFIG"; then
  [[ -s "$ORIGEM" ]] || {
    echo "Arquivo $ORIGEM não encontrado. Crie manualmente $CONFIG com as propriedades reais de produção." >&2
    echo "Não continue enquanto a configuração do banco/JWT/ADMIN não estiver disponível fora do JAR." >&2
    exit 1
  }
  sudo -n install -d -m 0700 /etc/foodbridge
  sudo -n install -m 0600 -- "$ORIGEM" "$CONFIG"
  echo "Configuração copiada para $CONFIG com permissões 0600."
else
  echo "Mantendo configuração de produção já existente: $CONFIG"
fi

sudo -n install -d -m 0755 /etc/systemd/system/foodbridge.service.d
printf '%s\n' '[Service]' 'Environment="SPRING_CONFIG_ADDITIONAL_LOCATION=file:/etc/foodbridge/"' |
  sudo -n tee "$OVERRIDE" > /dev/null
sudo -n chmod 0644 "$OVERRIDE"
sudo -n systemctl daemon-reload

echo "Pré-configuração pronta. Nenhum banco foi modificado e o serviço NÃO foi reiniciado."
echo "Verifique: sudo systemctl cat foodbridge.service"
echo "Depois, revise os secrets e vars do GitHub descritos em docs/DEPLOY_ORACLE.md."
