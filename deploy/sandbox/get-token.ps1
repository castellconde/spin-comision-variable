# Obtiene un token del realm 'applications' via port-forward a Keycloak.
# Uso:  $t = .\deploy\sandbox\get-token.ps1 ;  $t
$ErrorActionPreference = "Stop"
Write-Host "==> port-forward a Keycloak (localhost:8080)..." -ForegroundColor Cyan
$pf = Start-Process oc -ArgumentList "port-forward","svc/keycloak","8080:8080" -NoNewWindow -PassThru
Start-Sleep -Seconds 4
try {
    $body = @{ grant_type="client_credentials"; client_id="pos-client"; client_secret="pos-secret" }
    $resp = Invoke-RestMethod -Method Post `
        -Uri "http://localhost:8080/realms/applications/protocol/openid-connect/token" `
        -ContentType "application/x-www-form-urlencoded" -Body $body
    $resp.access_token
} finally {
    Stop-Process -Id $pf.Id -ErrorAction SilentlyContinue
}
