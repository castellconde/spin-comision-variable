# Despliegue en OpenShift Developer Sandbox (gratis)

Prueba el piloto end-to-end en un cluster OpenShift real, sin costo, con build
**en el cluster** desde GitHub. El stack completo (Postgres + Keycloak + mock
Spin + microservicio) corre en tu proyecto del sandbox.

## 1. Requisitos

1. Cuenta gratuita (30 días, renovable): https://developers.redhat.com/developer-sandbox
2. `oc` CLI instalado.
3. Iniciar sesión: en la consola del sandbox, menú de usuario → **Copy login command** → copia y ejecuta el `oc login --token=... --server=...`.

## 2. Despliegue en un comando

```bash
bash deploy/sandbox/deploy-sandbox.sh
```

El script:
1. Lanza `oc new-build --strategy=docker` desde el repo de GitHub (compila el `Dockerfile` multi-stage en el cluster — tarda varios minutos la primera vez).
2. Crea ConfigMaps con el realm de Keycloak y las mappings del mock Spin.
3. Aplica config/secret, las dependencias y el microservicio.
4. Espera los rollouts e imprime la Route pública.

> Build JVM (no nativo): el build nativo consume demasiada RAM para los límites del sandbox (~7 GB). Se desplian 1 réplica por componente y Redis queda deshabilitado (es opcional).

## 3. Probar

```bash
ROUTE=$(oc get route comision-variable -o jsonpath='{.spec.host}')
TOKEN=$(bash deploy/sandbox/get-token.sh)

curl -s -X POST "https://$ROUTE/rest/v1/comision-variable/comision" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"id":"POS-1","plaza":"PLZ-001","tienda":"TND-04521","caja":"CAJA-03","transaccion":"TRX-1","servicio":"SERV-TAE-ATT"}'
```

- Swagger UI: `https://$ROUTE/swagger-ui`
- Health: `https://$ROUTE/q/health/ready`
- Forzar fallo (reintentos/circuit breaker): enviar `"transaccion":"FORCE-ERROR"`.

## 4. Limpieza

```bash
oc delete all,configmap,secret,route -l app=comision-variable
oc delete all -l app=postgres
oc delete all,configmap -l app=keycloak
oc delete all,configmap -l app=spin-mock
oc delete bc/comision-variable is/comision-variable
```

## 5. Notas del sandbox

- Sin permisos de cluster-admin ni operadores: por eso Postgres/Keycloak/Spin se despliegan como contenedores simples (imágenes OpenShift-friendly), no como operadores.
- Imágenes con UID aleatorio (SCC `restricted-v2`): el `Dockerfile` corre como usuario no root con permisos de grupo root.
- Datos de Postgres efímeros (`emptyDir`) para no consumir cuota de PVC.
- El issuer del token no se valida estrictamente (`quarkus.oidc.token.issuer=any`), así que el token emitido vía port-forward funciona aunque el host difiera.
