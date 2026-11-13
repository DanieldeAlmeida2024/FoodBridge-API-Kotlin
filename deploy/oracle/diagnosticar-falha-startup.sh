#!/usr/bin/env bash
# Diagnostico de incidente API Oracle: nao altera processo, arquivo, firewall ou DB.
set -uo pipefail

echo '=== INCIDENT_API_STATUS ==='
for unit in foodbridge.service nginx docker; do
  state="$(systemctl is-active "$unit" 2>/dev/null || true)"
  printf 'UNIT_%s=%s\n' "$(echo "$unit" | tr '.-' '__')" "$state"
done
sudo -n systemctl show foodbridge.service -p ActiveState -p SubState -p Result -p ExecMainCode -p ExecMainStatus -p NRestarts -p ActiveEnterTimestamp -p ExecMainStartTimestamp -p TimeoutStartUSec -p TimeoutStopUSec -p Restart -p MemoryCurrent -p MemoryMax -p StandardOutput -p StandardError 2>/dev/null || true

echo '=== LISTENERS ==='
ss -H -lnt | awk '$4 ~ /:(80|8080|5432|8787)$/ {print "LISTEN=" $4}' || true

echo '=== PORT_HEALTH ==='
for pair in '80|/' '8080|/dashboards/publico' '8787|/health'; do
  port="${pair%%|*}"
  route="${pair#*|}"
  max=8
  [[ "$port" != "8080" ]] || max=25
  code="$(curl --noproxy '*' -sS -o /dev/null -w '%{http_code}' --connect-timeout 3 --max-time "$max" "http://127.0.0.1:$port$route" 2>/dev/null)"
  exitcode=$?
  echo "HTTP_${port}_CURL_EXIT=$exitcode HTTP_${port}_STATUS=$code"
done
if command -v pg_isready >/dev/null 2>&1; then
  pg_isready -h 127.0.0.1 -p 5432 >/dev/null 2>&1
  echo "POSTGRES_TCP_READY_EXIT=$?"
else
  echo 'POSTGRES_TCP_READY_EXIT=NOT_AVAILABLE'
fi

echo '=== KERNEL_CRASH_RESERVATION ==='
# Nao imprimir a command line inteira (pode conter parametros desconhecidos).
if grep -Eq '(^|[[:space:]])crashkernel=' /proc/cmdline; then
  echo 'CRASHKERNEL_ARG_PRESENT=YES'
else
  echo 'CRASHKERNEL_ARG_PRESENT=NO'
fi
kdump_status="$(systemctl is-active kdump 2>/dev/null || true)"
echo "KDUMP_SERVICE_STATUS=$kdump_status"

echo '=== HOST_CAPACITY ==='
df -P /opt/foodbridge | awk 'NR==2 {print "DISK_USE=" $5}'
awk '/^(MemTotal|MemAvailable|SwapTotal|SwapFree):/ {key=$1; sub(/:$/, "", key); print "MEMINFO_" key "_KB=" $2}' /proc/meminfo
if command -v swapon >/dev/null 2>&1; then
  swapon --noheadings --show=NAME,SIZE,USED --bytes 2>/dev/null | awk '{print "SWAP_DEVICE_PRESENT=YES"}' | head -n 1
fi
if sudo -n journalctl -k --since '2026-11-13 12:58:00 UTC' --no-pager -o cat >/dev/null 2>&1; then
  kernel_oom="$(sudo -n journalctl -k --since '2026-11-13 12:58:00 UTC' --no-pager -o cat 2>/dev/null | grep -Eic 'out of memory|killed process|oom-kill' || true)"
  echo "KERNEL_OOM_SIGNALS=$kernel_oom"
fi
ps -eo comm,%mem,%cpu,etime --no-headers | awk '$1 == "java" {print "JAVA_MEM_PCT=" $2, "JAVA_CPU_PCT=" $3, "JAVA_ETIME=" $4}' | head -n 5

echo '=== CURRENT_JAR_AND_BACKUPS ==='
jar='/opt/foodbridge/FoodBridge-API-Kotlin/build/libs/foodbridge-0.0.1-SNAPSHOT.jar'
backups='/opt/foodbridge/FoodBridge-API-Kotlin/deploy-backups'
if sudo -n test -s "$jar"; then
  sudo -n sha256sum "$jar" | awk '{print "CURRENT_JAR_SHA256=" $1}'
else
  echo 'CURRENT_JAR=NOT_FOUND'
fi
if sudo -n test -d "$backups"; then
  sudo -n find "$backups" -maxdepth 1 -type f -name '*.jar' -printf '%T@ %f\n' | sort -nr | head -n 5 | cut -d' ' -f2- | sed 's/^/RECENT_BACKUP=/'
  recent="$(sudo -n find "$backups" -maxdepth 1 -type f -name '*.jar' -printf '%T@ %p\n' | sort -nr | head -n 1 | cut -d' ' -f2-)"
  if [[ -n "$recent" ]]; then
    sudo -n sha256sum "$recent" | awk '{print "LATEST_BACKUP_SHA256=" $1}'
  fi
fi

echo '=== JOURNAL_CLASSIFIED_WITHOUT_RAW_LOG_TEXT ==='
sudo -n journalctl -u foodbridge.service --since '2026-11-13 12:58:00 UTC' --no-pager -n 1200 -o cat 2>/dev/null | python3 -c "$(cat <<'PY'
import collections
import re
import sys

rules = {
  "APP_RUN_FAILED": r"Application run failed",
  "DB_CONNECTION": r"Connection refused|connection attempt failed|could not connect|Failed to obtain JDBC|PSQLException|JDBCConnectionException",
  "DB_AUTH": r"password authentication failed|authentication failed|permission denied for",
  "SCHEMA_MISMATCH": r"Schema-validation|missing table|missing column|relation .* does not exist|schema validation",
  "BEAN_CREATION": r"BeanCreationException|UnsatisfiedDependencyException|Failed to start bean",
  "JAVA_OOM": r"OutOfMemoryError|Java heap space",
  "PORT_BIND": r"Address already in use|BindException",
  "CONFIG_ERROR": r"Could not resolve placeholder|PropertySourcesPlaceholderConfigurer|Failed to bind properties",
  "MIGRATION": r"FlywayException|LiquibaseException",
  "HIKARI_TIMEOUT": r"HikariPool.*(timed out|failed)|Connection is not available",
  "TERMINATED": r"Main process exited|SIGKILL|status=9/KILL",
}
counts = collections.Counter()
classes = collections.Counter()
sqlstates = collections.Counter()
for line in sys.stdin:
    for name, pat in rules.items():
        if re.search(pat, line, re.IGNORECASE):
            counts[name] += 1
    for cls in re.findall(r"\b(?:[a-zA-Z_$][\w$]*\.){0,12}[A-Za-z_$][\w$]*(?:Exception|Error)\b", line):
        # Emit only Java class names, not exception messages or configuration.
        classes[cls.split(".")[-1]] += 1
    for state in re.findall(r"SQLSTATE\s*[:=]\s*([A-Z0-9]{5})", line, re.IGNORECASE):
        sqlstates[state] += 1
print("JOURNAL_ERRORS_BY_CATEGORY=" + ",".join(f"{k}:{v}" for k,v in sorted(counts.items())) if counts else "JOURNAL_ERRORS_BY_CATEGORY=NONE_MATCHED")
print("EXCEPTION_CLASS_NAMES=" + ",".join(f"{k}:{v}" for k,v in classes.most_common(15)) if classes else "EXCEPTION_CLASS_NAMES=NONE")
print("SQLSTATE_CODES=" + ",".join(f"{k}:{v}" for k,v in sqlstates.most_common(10)) if sqlstates else "SQLSTATE_CODES=NONE")
PY
)"
echo 'INCIDENT_DIAGNOSTIC_FINISHED=YES (somente leitura)'
