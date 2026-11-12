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
# Não criar configuração/override para service que não usa o JAR esperado.
DESTINO="$ROOT/build/libs/foodbridge-0.0.1-SNAPSHOT.jar"
UNIT_EXEC="$(sudo -n systemctl show foodbridge.service -p ExecStart --value)"
UNIT_DIR="$(sudo -n systemctl show foodbridge.service -p WorkingDirectory --value)"
if [[ "$UNIT_EXEC" != *"$DESTINO"* ]]; then
  [[ "$UNIT_DIR" == "$ROOT" &&
     "$UNIT_EXEC" == *"build/libs/foodbridge-0.0.1-SNAPSHOT.jar"* ]] || {
    echo "O ExecStart atual não usa o JAR esperado; revise o systemd antes de preparar." >&2
    exit 1
  }
fi
unset UNIT_EXEC UNIT_DIR

if ! sudo -n test -s "$CONFIG"; then
  [[ -s "$ORIGEM" ]] || {
    echo "Arquivo $ORIGEM não encontrado. Crie manualmente $CONFIG com as propriedades reais de produção." >&2
    echo "Não continue enquanto a configuração do banco/JWT/ADMIN não estiver disponível fora do JAR." >&2
    exit 1
  }
  sudo -n install -d -o root -g opc -m 0750 /etc/foodbridge
  sudo -n install -o root -g opc -m 0640 -- "$ORIGEM" "$CONFIG"
  echo "Configuração copiada para $CONFIG com permissões root:opc 0640."
else
  echo "Mantendo configuração de produção já existente: $CONFIG"
fi

# O unit systemd existente roda como User=opc: permitir leitura apenas a esse grupo.
[[ "$(sudo -n systemctl show foodbridge.service -p User --value)" == "opc" ]] || {
  echo "foodbridge.service não roda como opc. Revise o usuário do serviço antes do deploy." >&2
  exit 1
}
sudo -n chown root:opc /etc/foodbridge "$CONFIG"
sudo -n chmod 0750 /etc/foodbridge
sudo -n chmod 0640 "$CONFIG"
sudo -n -u opc test -r "$CONFIG" || {
  echo "A API (usuário opc) não consegue ler $CONFIG." >&2
  exit 1
}

sudo -n install -d -m 0755 /etc/systemd/system/foodbridge.service.d
printf '%s\n' '[Service]' 'Environment="SPRING_CONFIG_ADDITIONAL_LOCATION=file:/etc/foodbridge/"' |
  sudo -n tee "$OVERRIDE" > /dev/null
sudo -n chmod 0644 "$OVERRIDE"
sudo -n systemctl daemon-reload

echo "Pré-configuração pronta. Nenhum banco foi modificado e o serviço NÃO foi reiniciado."
echo "Verifique: sudo systemctl cat foodbridge.service"
echo "Depois, revise os secrets e vars do GitHub descritos em docs/DEPLOY_ORACLE.md."
