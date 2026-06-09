#!/usr/bin/env bash
# Obtiene un token del realm 'applications' usando el Service de Keycloak vía
# port-forward (no requiere exponer Keycloak con Route).
set -euo pipefail
echo "==> port-forward a Keycloak (localhost:8080)..."
oc port-forward svc/keycloak 8080:8080 >/dev/null 2>&1 &
PF_PID=$!
sleep 4
TOKEN=$(curl -s -X POST \
  "http://localhost:8080/realms/applications/protocol/openid-connect/token" \
  -d grant_type=client_credentials -d client_id=pos-client -d client_secret=pos-secret \
  | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')
kill $PF_PID 2>/dev/null || true
if [ -z "$TOKEN" ]; then echo "ERROR: no se obtuvo token"; exit 1; fi
echo "$TOKEN"
