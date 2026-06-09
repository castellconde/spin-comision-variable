# =====================================================================
#  Despliegue del piloto Comision Variable en OpenShift Developer Sandbox
#  Version PowerShell (Windows). Equivalente a deploy-sandbox.sh.
#
#  Requisitos:
#    1. Cuenta: https://developers.redhat.com/developer-sandbox
#    2. 'oc' (oc.exe) en el PATH y logueado: copia el 'oc login --token=...'
#       desde la consola del sandbox (boton "Copy login command").
#
#  Uso (PowerShell, desde cualquier carpeta):
#    .\deploy\sandbox\deploy-sandbox.ps1
#    # repo distinto:  $env:GIT_URL="https://github.com/ORG/REPO.git"; .\deploy\sandbox\deploy-sandbox.ps1
# =====================================================================
$ErrorActionPreference = "Stop"

$GitUrl = if ($env:GIT_URL) { $env:GIT_URL } else { "https://github.com/castellconde/spin-comision-variable.git" }
$App = "comision-variable"
# Raiz del repo (este script vive en deploy/sandbox/)
$Root = (Resolve-Path "$PSScriptRoot\..\..").Path
Set-Location $Root

Write-Host "==> Verificando sesion oc..." -ForegroundColor Cyan
oc whoami | Out-Null
if ($LASTEXITCODE -ne 0) { throw "No hay sesion. Ejecuta el 'oc login --token=...' del sandbox." }
$Ns = (oc project -q)
Write-Host "    Namespace actual: $Ns"

Write-Host "==> 1/5 Build en el cluster desde GitHub..." -ForegroundColor Cyan
oc get bc/$App 2>$null | Out-Null
if ($LASTEXITCODE -ne 0) {
    oc new-build --strategy=docker --name=$App $GitUrl
} else {
    Write-Host "    BuildConfig ya existe; iniciando nuevo build."
    oc start-build $App
}
Write-Host "    Siguiendo logs del build (puede tardar varios minutos)..."
oc logs -f bc/$App

Write-Host "==> 2/5 ConfigMaps (realm Keycloak + mappings mock Spin)..." -ForegroundColor Cyan
oc create configmap keycloak-realm --from-file=realm-applications.json=deploy/keycloak/realm-applications.json --dry-run=client -o yaml | oc apply -f -
oc create configmap spin-mock-mappings --from-file=mock-spin/mappings --dry-run=client -o yaml | oc apply -f -

Write-Host "==> 3/5 Config + Secret de la app..." -ForegroundColor Cyan
oc apply -f deploy/sandbox/00-config.yaml

Write-Host "==> 4/5 Dependencias (PostgreSQL + Keycloak + mock Spin)..." -ForegroundColor Cyan
oc apply -f deploy/sandbox/10-postgres.yaml
oc apply -f deploy/sandbox/20-keycloak.yaml
oc apply -f deploy/sandbox/30-spin-mock.yaml
oc rollout status deploy/postgres  --timeout=180s
oc rollout status deploy/spin-mock  --timeout=180s
oc rollout status deploy/keycloak   --timeout=300s

Write-Host "==> 5/5 Microservicio + Service + Route..." -ForegroundColor Cyan
oc apply -f deploy/sandbox/40-comision-variable.yaml
oc rollout status deploy/$App --timeout=300s

$Route = (oc get route $App -o jsonpath='{.spec.host}')
Write-Host ""
Write-Host "================================================================" -ForegroundColor Green
Write-Host " Despliegue terminado."
Write-Host "   API:        https://$Route/rest/v1/comision-variable/comision"
Write-Host "   Swagger UI: https://$Route/swagger-ui"
Write-Host "   Health:     https://$Route/q/health/ready"
Write-Host ""
Write-Host " Token de prueba:  .\deploy\sandbox\get-token.ps1"
Write-Host "================================================================" -ForegroundColor Green
