#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
ENV_FILE="${DEPLOY_DIR}/.env.docker"
OUT_DIR="${DEPLOY_DIR}/backups"

if [ ! -f "${ENV_FILE}" ]; then
  echo "Missing ${ENV_FILE}. Copy .env.docker.example first."
  exit 1
fi

mkdir -p "${OUT_DIR}"
source "${ENV_FILE}"

ts="$(date +%Y%m%d-%H%M%S)"
out="${OUT_DIR}/postgres-${POSTGRES_DB}-${ts}.sql.gz"

docker compose --env-file "${ENV_FILE}" -f "${DEPLOY_DIR}/docker-compose.prod.yml" exec -T db \
  pg_dump -U "${POSTGRES_USER}" "${POSTGRES_DB}" | gzip > "${out}"

echo "Backup created: ${out}"
