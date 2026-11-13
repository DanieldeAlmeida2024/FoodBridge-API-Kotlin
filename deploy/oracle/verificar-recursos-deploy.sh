#!/usr/bin/env bash
# Gate de recursos para release de API (somente leitura; executa ANTES do upload/restart).
set -Eeuo pipefail

[[ -r /proc/meminfo ]] || { echo 'DEPLOY_CAPACITY=BLOCKED motivo=meminfo_indisponivel' >&2; exit 1; }
total_kib="$(awk '/^MemTotal:/ {print $2; exit}' /proc/meminfo)"
available_kib="$(awk '/^MemAvailable:/ {print $2; exit}' /proc/meminfo)"
[[ "$total_kib" =~ ^[0-9]+$ && "$available_kib" =~ ^[0-9]+$ ]] || {
  echo 'DEPLOY_CAPACITY=BLOCKED motivo=memoria_nao_numerica' >&2
  exit 1
}

# O stack de producao concentra Spring Boot/JVM, PostgreSQL, Nginx e Puppeteer/Docker.
# Minimos de seguranca, nao recomendacao de dimensionamento. Preferir >= 2 GiB reais.
readonly MIN_TOTAL_KIB=1048576   # 1 GiB fisico
readonly MIN_AVAILABLE_KIB=262144 # 256 MiB disponiveis para restart

echo "RAM_TOTAL_MIB=$((total_kib / 1024))"
echo "RAM_AVAILABLE_MIB=$((available_kib / 1024))"

if (( total_kib < MIN_TOTAL_KIB )); then
  echo 'DEPLOY_CAPACITY=BLOCKED motivo=ram_fisica_inferior_1GiB (recomendar >= 2GiB para este stack)' >&2
  exit 1
fi
if (( available_kib < MIN_AVAILABLE_KIB )); then
  echo 'DEPLOY_CAPACITY=BLOCKED motivo=memoria_disponivel_inferior_256MiB' >&2
  exit 1
fi

echo 'DEPLOY_CAPACITY=PASS (somente leitura)'
