#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
export SPRING_PROFILES_ACTIVE=dev
export SERVER_PORT="${SERVER_PORT:-8080}"
exec mvn -q spring-boot:run
