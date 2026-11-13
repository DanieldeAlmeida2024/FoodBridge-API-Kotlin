#!/usr/bin/env bash
# Telemetria read-only de recursos para VM.Standard.E2.1.Micro.
# Nao bloquear publicacoes por MemTotal: a shape reconhece ~945 MiB apos desativar Kdump.
# Integridade do JAR, SSH, CI, health e rollback permanecem obrigatorios.
set -Eeuo pipefail

if [[ ! -r /proc/meminfo ]]; then
  echo 'DEPLOY_CAPACITY=WARNING motivo=meminfo_indisponivel'
  exit 0
fi

total_kib="$(awk '/^MemTotal:/ {print $2; exit}' /proc/meminfo)"
available_kib="$(awk '/^MemAvailable:/ {print $2; exit}' /proc/meminfo)"
swap_total_kib="$(awk '/^SwapTotal:/ {print $2; exit}' /proc/meminfo)"
swap_free_kib="$(awk '/^SwapFree:/ {print $2; exit}' /proc/meminfo)"
if [[ ! "$total_kib" =~ ^[0-9]+$ || ! "$available_kib" =~ ^[0-9]+$ ]]; then
  echo 'DEPLOY_CAPACITY=WARNING motivo=memoria_nao_numerica'
  exit 0
fi

echo "RAM_TOTAL_MIB=$((total_kib / 1024))"
echo "RAM_AVAILABLE_MIB=$((available_kib / 1024))"
if [[ "$swap_total_kib" =~ ^[0-9]+$ && "$swap_free_kib" =~ ^[0-9]+$ ]]; then
  echo "SWAP_USED_MIB=$(((swap_total_kib - swap_free_kib) / 1024))"
fi

# Registrar risco operacional em vez de impedir deploy numa shape de 1 GB.
if (( total_kib < 1048576 )); then
  echo 'DEPLOY_CAPACITY=WARNING motivo=oracle_micro_ram_fisica_abaixo_1GiB'
fi
if (( available_kib < 131072 )); then
  echo 'DEPLOY_CAPACITY=WARNING motivo=memoria_disponivel_abaixo_128MiB'
fi

echo 'DEPLOY_CAPACITY=PASS modo=telemetria_micro_sem_bloqueio'
