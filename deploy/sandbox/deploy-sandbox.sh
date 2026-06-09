#!/usr/bin/env bash
# =====================================================================
#  Despliegue del piloto Comisión Variable en OpenShift Developer Sandbox.
#  Build EN EL CLUSTER desde GitHub (Docker strategy, multi-stage).
#
#  Requisitos previos:
#    1. Cuenta gratuita: https://developers.redhat.com/developer-sandbox
#    2. 'oc' instalado y logueado: copia el comando 'oc login --token=... --server=...'
#       desde la consola del sandbox (botón "Copy login command").
#
#  Uso:
#    bash deploy/sandbox/deploy-sandbox.sh
# =====================================================================
set -euo pipefail

GIT_URL="${GIT_URL:-https://github.com/castellconde/spin-comision-variable.git}"
APP="comision-variable"
# Raíz del repo (este script vive en deploy/sandbox/)
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

echo "==> Verificando sesión oc..."
oc whoami >/dev/null 2>&1 || { echo "ERROR: no hay sesión. Ejecuta el 'oc login --token=...' del sandbox."; exit 1; }
NS="$(oc project -q)"
echo "    Proyecto/namespace actual: $NS"

echo "==> 1/5 Build en el cluster desde GitHub (si no existe)..."
if ! oc get bc/$APP >/dev/null 2>&1; then
  oc new-build --strategy=docker --name=$APP \
    --context-dir=comision-variable \
    "$GIT_URL"
else
  echo "    BuildConfig ya existe; iniciando nuevo build."
  oc start-build $APP
fi
echo "    Siguiendo logs del build (puede tardar varios minutos)..."
oc logs -f bc/$APP || true

echo "==> 2/5 ConfigMaps de realm Keycloak y mappings del mock Spin..."
oc create configmap keycloak-realm \
  --from-file=realm-applications.json=deploy/keycloak/realm-applications.json \
  --dry-run=client -o yaml | oc apply -f -
oc create configmap spin-mock-mappings \
  --from-file=mock-spin/mappings \
  --dry-run=client -o yaml | oc apply -f -

echo "==> 3/5 Config + Secret de la app..."
oc apply -f deploy/sandbox/00-config.yaml

echo "==> 4/5 Dependencias (PostgreSQL + Keycloak + mock Spin)..."
oc apply -f deploy/sandbox/10-postgres.yaml
oc apply -f deploy/sandbox/20-keycloak.yaml
oc apply -f deploy/sandbox/30-spin-mock.yaml
echo "    Esperando dependencias..."
oc rollout status deploy/postgres   --timeout=180s || true
oc rollout status deploy/spin-mock   --timeout=180s || true
oc rollout status deploy/keycloak    --timeout=300s || true

echo "==> 5/5 Microservicio + Service + Route..."
oc apply -f deploy/sandbox/40-comision-variable.yaml
oc rollout status deploy/$APP --timeout=300s || true

ROUTE="$(oc get route $APP -o jsonpath='{.spec.host}' 2>/dev/null || true)"
echo
echo "================================================================"
echo " Despliegue terminado."
echo "   API:        https://$ROUTE/rest/v1/comision-variable/comision"
echo "   Swagger UI:  https://$ROUTE/swagger-ui"
echo "   Health:      https://$ROUTE/q/health/ready"
echo
echo " Token de prueba (cliente pos-client del realm applications):"
echo "   bash deploy/sandbox/get-token.sh"
echo "================================================================"
