#!/usr/bin/env bash
# FoodBridge: VM busca artefatos CI aprovados por HTTPS; nao depende de SSH do runner.
set -Eeuo pipefail
umask 077
MODE="${1:-}"
ACTION="${2:---check}"
[[ "$MODE" == "api" || "$MODE" == "frontend" ]] || { echo "Uso: $0 api|frontend --check|--apply" >&2; exit 2; }
[[ "$ACTION" == "--check" || "$ACTION" == "--apply" ]] || exit 2
HERE="$(cd "$(dirname "$0")" && pwd)"
STATE="/var/lib/foodbridge-deploy"
TOKEN_FILE="/etc/foodbridge/actions-read.token"
[[ -d "$STATE" && -w "$STATE" && -s "$TOKEN_FILE" && -r "$TOKEN_FILE" ]] || { echo "Estado/token nao preparado" >&2; exit 1; }
for cmd in gh flock sha256sum; do command -v "$cmd" >/dev/null || { echo "Falta $cmd" >&2; exit 1; }; done
exec 9>"$STATE/.lock-$MODE"
flock -n 9 || { echo "Outra execucao ativa"; exit 0; }
export GH_TOKEN
GH_TOKEN="$(tr -d '\r\n' < "$TOKEN_FILE")"
export GH_PAGER=cat
if [[ "$MODE" == "api" ]]; then
  REPO="DanieldeAlmeida2024/FoodBridge-API-Kotlin"
  WORKFLOW="foodbridge-ci-cd.yml"
  ARTIFACT="foodbridge-api-jar"
else
  REPO="DanieldeAlmeida2024/Foodbridge-Frontend-React"
  WORKFLOW="ci.yml"
  ARTIFACT="foodbridge-frontend-dist"
fi
HEAD="$(gh api "repos/$REPO/commits/main" --jq '.sha')"
[[ "$HEAD" =~ ^[0-9a-f]{40}$ ]] || exit 1
RUN="$(gh run list --repo "$REPO" --workflow "$WORKFLOW" --branch main --event push --status success --limit 25 --json databaseId,headSha --jq ".[] | select(.headSha == \"$HEAD\") | .databaseId" | head -n 1)"
[[ "$RUN" =~ ^[0-9]+$ ]] || { echo "$MODE: CI de push da main ainda nao aprovado"; exit 0; }
STAMP="$STATE/$MODE.sha"
[[ ! -s "$STAMP" || "$(cat "$STAMP")" != "$HEAD" ]] || { echo "$MODE: versao ja publicada"; exit 0; }
TMP="$(mktemp -d "/tmp/foodbridge-approved-$MODE.XXXXXXXX")"
UPLOAD=""
trap '[[ -z "$UPLOAD" ]] || rm -f -- "$UPLOAD"; rm -rf -- "$TMP"' EXIT
mkdir -p "$TMP/package"
gh run download "$RUN" --repo "$REPO" --name "$ARTIFACT" --dir "$TMP/package"
if [[ "$MODE" == "api" ]]; then
  FILE="$TMP/package/foodbridge-0.0.1-SNAPSHOT.jar"
  test -s "$FILE"
else
  test -s "$TMP/package/index.html"
  [[ -z "$(find "$TMP/package" -type l -print -quit)" ]] || { echo "Symlinks rejeitados" >&2; exit 1; }
fi
[[ "$(gh api "repos/$REPO/commits/main" --jq '.sha')" == "$HEAD" ]] || { echo "Main mudou, abortando" >&2; exit 1; }
echo "$MODE: RUN=$RUN SHA=$HEAD pacote validado"
[[ "$ACTION" == "--apply" ]] || { echo "CHECK PASS (sem publicar)"; exit 0; }
[[ "${FOODBRIDGE_PULL_DEPLOY_ENABLED:-false}" == "true" ]] || { echo "DEPLOY_DESATIVADO"; exit 0; }

if [[ "$MODE" == "api" ]]; then
  test -f "$HERE/atualizar-api.sh"
  test -f "$HERE/verificar-vm.sh"
  bash "$HERE/verificar-vm.sh"
  UPLOAD="$(mktemp /tmp/foodbridge-deploy.XXXXXXXX.jar)"
  install -m 0600 "$FILE" "$UPLOAD"
  CHECKSUM="$(sha256sum "$UPLOAD" | awk '{print $1}')"
  bash "$HERE/atualizar-api.sh" "$CHECKSUM" "$UPLOAD"
else
  BASE="/opt/foodbridge/foodbridge-frontend/foodbridge-frontend"
  TARGET="$BASE/dist"
  [[ -d "$TARGET" && -w "$BASE" ]] || { echo "Diretorio frontend inacessivel" >&2; exit 1; }
  sudo -n nginx -t
  STAGE="$(mktemp -d "$BASE/.dist-next.XXXXXXXX")"
  cp -a "$TMP/package/." "$STAGE/"
  test -s "$STAGE/index.html"
  sudo -n restorecon -RF "$STAGE"
  mkdir -p "$BASE/backups"
  BACKUP="$BASE/backups/actions-$HEAD-$(date -u +%Y%m%dT%H%M%SZ)"
  mv "$TARGET" "$BACKUP"
  if ! mv "$STAGE" "$TARGET"; then
    mv "$BACKUP" "$TARGET" || true
    echo "Falha na troca de dist; verificar backup" >&2
    exit 1
  fi
  if ! sudo -n restorecon -RF "$TARGET" || ! sudo -n nginx -t || [[ ! -s "$TARGET/index.html" ]]; then
    mv "$TARGET" "$BASE/.dist-failed-$(date -u +%s)" || true
    mv "$BACKUP" "$TARGET" || { echo "ROLLBACK FALHOU" >&2; exit 1; }
    sudo -n restorecon -RF "$TARGET" || true
    echo "Rollback frontend aplicado" >&2
    exit 1
  fi
  echo "Frontend publicado, backup=$BACKUP"
fi
printf '%s\n' "$HEAD" > "$STAMP.tmp.$$"
mv "$STAMP.tmp.$$" "$STAMP"
echo "$MODE: DEPLOY_OK $HEAD"
