# RUNBOOK
## Sistema de Trabajo TI

## Control de Versiones

| No. de Versión | Fecha de cambio | Autor | Comentarios |
|---|---|---|---|
| 0.1 | 08/06/2026 | Roberto Castillo | Runbook inicial del piloto Comisión Variable. |
| 0.2 | 08/06/2026 | Roberto Castillo | Agregada sección 4.5: arranque paso a paso en OpenShift Developer Sandbox. |
| 0.3 | 08/06/2026 | Roberto Castillo | Sección 4.5: agregado paso de clonación del repo en terminal (Web Terminal / local) y nota de estructura aplanada. |
| 0.4 | 08/06/2026 | Roberto Castillo | Agregada sección 4.6: obtener token vía Route de Keycloak (sin script). |

## 1. Identificación del Producto

| Campo | Valor |
|---|---|
| Producto | Piloto SPIN — Comisión Variable |
| Dueño de aplicación | [PENDIENTE: correo del dueño funcional] |
| Plataforma | OpenShift ROSA / ARO (namespace `spin`) |
| Líder de Proyecto TI | [PENDIENTE] |
| Desarrollador | [PENDIENTE] |
| Líder Técnico | Roberto Castillo <roberto.castillo@oxxo.com> |

### 1.1 Referencias

| Nombre del Documento | Tipo de Documento |
|---|---|
| Especificación Técnica | Esp_Tecnica_v0.1.docx |
| Análisis de Riesgo Aplicativo | ARA_v0.1.docx |
| Modelado de Amenazas | MDA_v0.1.docx (+ .tm7 [PENDIENTE]) |
| Principios de Seguridad | Principios_Seguridad_v0.1.docx |

## 2. Presentación del Producto

### 2.1 Objetivo

Servicio de solo consulta que expone la comisión variable consumiendo el API de Spin, manteniendo trazabilidad, seguridad y resiliencia.

### 2.2 Alcance

Consulta de comisión por plaza, tienda, caja, transacción y producto/servicio. Stateless; escala horizontalmente. Estado externo en PostgreSQL (bitácora) y Redis opcional (cache de token).

### 2.3 Sistemas Involucrados

POS (consumidor), Keycloak (realm `applications`), API Spin (proveedor), PostgreSQL (bitácora/config), Redis (cache opcional), Datadog (observabilidad), Conjur (secretos).

### 2.4 Definiciones, Acrónimos y Abreviaciones

POS = Punto de Venta; CB = Circuit Breaker; OIDC = OpenID Connect; Conjur = CyberArk Conjur.

## 3. Respaldo y depuración de información

### 3.1 Política de respaldos

PostgreSQL gestionado (RDS/operador). [PENDIENTE: política de respaldo corporativa — pg_dump / snapshots].

### 3.2 Depuración de logs

Logs en stdout (JSON) recolectados por el agente Datadog. Retención según política de Datadog. [PENDIENTE: confirmar retención].

### 3.3 Depuración de datos transaccionales

Tabla `bitacora_consumo` particionada por mes. El job `PartitionMaintenanceJob` purga particiones > 6 meses y crea 12 meses futuros (diario). Ejecución manual:

```sql
SELECT crear_particiones_futuras(12);
SELECT purgar_particiones_antiguas(6);
```

## 4. Ejecución del producto

### 4.1 Arranque normal

```bash
oc new-project spin
oc apply -k deploy/openshift/
oc rollout status deploy/comision-variable
```

Build de imagen (nativa o JVM):

```bash
./mvnw package -Dnative
docker build -f src/main/docker/Dockerfile.native -t comision-variable:1.0.0 .
# alterno JVM:
./mvnw package && docker build -f src/main/docker/Dockerfile.jvm -t comision-variable:1.0.0 .
```

### 4.2 Detención normal

```bash
oc scale deploy/comision-variable --replicas=0
```

### 4.3 Variables de ambiente requeridas

Inyectadas por Conjur (secretas) o ConfigMap (no sensibles):

`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`*, `DB_PASSWORD`*, `KEYCLOAK_URL`, `KEYCLOAK_REALM`, `KEYCLOAK_CLIENT_ID`, `KEYCLOAK_CLIENT_SECRET`*, `SPIN_API_URL`, `SPIN_AUTH_URL`, `SPIN_CLIENT_ID`*, `SPIN_CLIENT_SECRET`*, `SPIN_API_KEY`*, `SPIN_RETRY_MAX`, `SPIN_CB_FAILURE_RATIO`, `SPIN_CB_DELAY_MS`, `SPIN_TIMEOUT_MS`, `SPIN_RATELIMIT_VALUE`, `SPIN_RATELIMIT_WINDOW_MS`, `REDIS_ENABLED`, `REDIS_URL`, `DD_ENV`, `OTEL_ENABLED`, `OTEL_EXPORTER_OTLP_ENDPOINT`.

(*) Provistas por Conjur.

### 4.4 Endpoints de salud

- `GET /q/health/live` -> 200 `{"status":"UP"}`
- `GET /q/health/ready` -> 200 cuando está listo para tráfico
- `GET /q/health/started` -> startup probe

### 4.5 Arranque en OpenShift Developer Sandbox (paso a paso)

Entorno gratuito para validar el piloto end-to-end. El build se hace **en el cluster** desde GitHub; no se requiere Docker local.

**Prerrequisitos**

1. Tener el código en GitHub con el `Dockerfile` multi-stage y la carpeta `deploy/sandbox/` (hacer `git push` antes de empezar).
2. Instalar la CLI `oc` (Red Hat OpenShift CLI).

**Paso 1 — Crear la cuenta del Sandbox.** Entrar a `https://developers.redhat.com/developer-sandbox`, iniciar sesión con la cuenta Red Hat (o crearla; es gratis, 30 días renovables) y pulsar **"Start your sandbox for free"**. Esperar a que se aprovisione.

**Paso 2 — Abrir la consola y lanzar OpenShift.** En la página del Sandbox pulsar **"Launch"** en la tarjeta de Red Hat OpenShift. El proyecto/namespace asignado tiene el formato `<usuario>-dev`.

**Paso 3 — Copiar el comando de login.** En la consola, esquina superior derecha → menú con tu usuario → **"Copy login command"** → **"Display Token"** → copiar el comando:

```bash
oc login --token=sha256~XXXXXXXX --server=https://api.CLUSTER.openshiftapps.com:6443
```

**Paso 4 — Iniciar sesión desde tu terminal.** Pegar y ejecutar ese comando. Verificar:

```bash
oc whoami
oc project -q
```

**Paso 5 — Clonar el repositorio en la terminal.** El script necesita los archivos del repo localmente (manifiestos, realm, mappings) para aplicarlos.

> En la **Web Terminal** de OpenShift (ícono `>_` arriba en la consola → *Start*) ya tienes `oc` autenticado a tu namespace y `git` disponibles, así que puedes **omitir los Pasos 3 y 4**. En una terminal local, primero completa el login (Pasos 3-4) y ten `git` instalado.

```bash
git clone https://github.com/castellconde/spin-comision-variable.git
cd spin-comision-variable
```

Si ya lo habías clonado antes, sólo actualízalo:

```bash
cd spin-comision-variable && git pull
```

El repositorio está **aplanado**: la raíz contiene `Dockerfile`, `pom.xml`, `src/`, `deploy/`, `mock-spin/`, etc. (no hay subcarpeta `comision-variable/`). Tras el `cd` ya estás en la raíz correcta.

**Paso 6 — Desplegar el servicio (un comando).** Desde la raíz del repo (donde quedaste tras el `cd`):

```bash
bash deploy/sandbox/deploy-sandbox.sh
```

El script lanza el build desde GitHub (`oc new-build`, tarda varios minutos la primera vez), crea los ConfigMaps del realm Keycloak y del mock Spin, aplica config/secret, despliega PostgreSQL + Keycloak + mock Spin y, por último, el microservicio con su Route. Imprime la URL pública al terminar. Si el repo es distinto al default, pasar `GIT_URL=https://github.com/ORG/REPO.git` antes del comando.

**Paso 7 — Verificar que arrancó.**

```bash
oc get pods
oc rollout status deploy/comision-variable
ROUTE=$(oc get route comision-variable -o jsonpath='{.spec.host}')
curl -s https://$ROUTE/q/health/ready
```

**Paso 8 — Probar el endpoint.**

```bash
TOKEN=$(bash deploy/sandbox/get-token.sh)
curl -s -X POST "https://$ROUTE/rest/v1/comision-variable/comision" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"id":"POS-1","plaza":"PLZ-001","tienda":"TND-04521","caja":"CAJA-03","transaccion":"TRX-1","servicio":"SERV-TAE-ATT"}'
```

Swagger UI: `https://$ROUTE/swagger-ui`.

**Rearranque / redeploy tras cambios.**

```bash
oc start-build comision-variable --follow
oc rollout restart deploy/comision-variable
```

**Detener para ahorrar cuota.**

```bash
oc scale deploy/comision-variable postgres keycloak spin-mock --replicas=0
```

**Problemas comunes (Sandbox)**

| Síntoma | Acción |
|---|---|
| `oc login` expira | El token del Sandbox dura ~24 h; repetir Paso 3-4. |
| Build falla por memoria | Confirmar empaquetado JVM (no nativo) en el `Dockerfile`. |
| App CrashLoopBackOff por OIDC | Esperar a que Keycloak esté Ready; `oc rollout restart deploy/comision-variable`. |
| Pod Pending por cuota | Bajar réplicas o `requests` de memoria; el Sandbox limita ~7 GB. |

### 4.6 Obtener token vía Route de Keycloak (sin script)

Alternativa a `get-token.sh`: exponer Keycloak con una Route y pedir el token directo al endpoint OIDC estándar desde curl, Postman o el navegador (sin `port-forward` ni script).

**Paso 1 — Exponer Keycloak con una Route (edge TLS).** Una sola vez:

```bash
oc create route edge keycloak --service=keycloak --port=8080
KC=$(oc get route keycloak -o jsonpath='{.spec.host}')
echo "https://$KC"
```

**Paso 2 — Pedir el token al endpoint OIDC.** Endpoint estándar del realm:

```
POST https://<keycloak-route>/realms/applications/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded
grant_type=client_credentials & client_id=pos-client & client_secret=pos-secret
```

Con curl:

```bash
curl -s -X POST "https://$KC/realms/applications/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d grant_type=client_credentials -d client_id=pos-client -d client_secret=pos-secret
```

Devuelve un JSON con `access_token` (vigencia ~5 min, `accessTokenLifespan` del realm).

**Paso 3 — Usar el token.** En Swagger UI (`/swagger-ui`) → *Authorize* → pega solo el `access_token`. O con curl al endpoint de negocio:

```bash
TOKEN="<access_token>"
ROUTE=$(oc get route comision-variable -o jsonpath='{.spec.host}')
curl -s -X POST "https://$ROUTE/rest/v1/comision-variable/comision" \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"id":"POS-1","plaza":"PLZ-001","tienda":"TND-04521","caja":"CAJA-03","transaccion":"TRX-1","servicio":"SERV-TAE-ATT"}'
```

**Paso 4 — (Opcional) Postman.** En el *environment* de la colección, fija `keycloak_url = https://<keycloak-route>` y ejecuta el request "1. Obtener token"; el resto de la colección reusa el `access_token`.

> Notas:
> - Keycloak ya está configurado para vivir detrás de la Route (`KC_HOSTNAME_STRICT=false`, `KC_PROXY_HEADERS=xforwarded`). Si algún flujo marcara error de hostname: `oc set env deploy/keycloak KC_HOSTNAME=https://$KC` y reiniciar.
> - **Seguridad:** exponer Keycloak con Route es adecuado para el piloto, pero deja el endpoint de token público; fuera del piloto, restringir su acceso.

## 5. Monitoreo y diagnóstico

### 5.1 Logs

JSON estructurado en stdout con `trace_id`, `span_id`, `correlation_id`. Buscar por `traceId` reportado en el `ErrorResponse`.

### 5.2 Métricas clave

Estado del circuit breaker hacia Spin, latencia de consulta (`latencia_ms` en bitácora), pool de conexiones JDBC, heap JVM (modo JVM).

### 5.3 Alertas configuradas

[PENDIENTE: alertas en Datadog — tasa de 5xx, circuito abierto, latencia p95].

## 6. Administración de la operación

### 6.1 Contactos de escalamiento

| Nivel | Contacto |
|---|---|
| L2 Aplicaciones | [PENDIENTE] |
| L3 DEV/PRD | [PENDIENTE] |
| Líder Técnico | Roberto Castillo <roberto.castillo@oxxo.com> |

### 6.2 Procedimientos de recuperación

| Modo de falla | Recuperación |
|---|---|
| 401 en el endpoint | Verificar `KEYCLOAK_*` y realm `applications`. |
| 502/503 `SPIN_*` | Revisar `SPIN_*`, conectividad y salud de Spin; esperar cierre del circuito. |
| 429 `SPIN_RATE_LIMITED` | Ajustar `SPIN_RATELIMIT_*` o reducir carga. |
| Pod no arranca (Flyway) | Revisar `DB_*` y logs de migración. |

### 6.3 Rollback

```bash
oc rollout undo deploy/comision-variable
```

Las migraciones Flyway son aditivas; el rollback de imagen no requiere revertir el esquema.

<!-- fin -->
