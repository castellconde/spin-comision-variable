# Mock Spin — Tabla de escenarios y administración (CRUD)

El mock (WireMock) simula al proveedor Spin. Cada **escenario** es un *stub
mapping* en `mappings/`, seleccionado por los datos del request que envía el
microservicio. Se administran en caliente con la admin API de WireMock (CRUD).

## Tabla de escenarios

El escenario se activa según el **disparador** en el body que el servicio manda a Spin
(derivado de la consulta del POS). Mayor prioridad = se evalúa primero.

| Escenario | Disparador (entrada) | Respuesta simulada | Prio | Archivo |
|---|---|---|---|---|
| Error 500 (interno) | `transaccion` contiene `ERR-500` o `FORCE-ERROR` | HTTP 500 → el servicio reintenta y abre el circuito | 1 | `escenario-error-500.json` |
| Error 404 (sin tarifa) | `transaccion` contiene `ERR-404` | HTTP 404 `TARIFA_NO_ENCONTRADA` | 1 | `escenario-error-404.json` |
| Error 422 (datos inválidos) | `transaccion` contiene `ERR-422` | HTTP 422 `DATOS_INVALIDOS` | 1 | `escenario-error-422.json` |
| Latencia / timeout | `transaccion` contiene `SLOW` | HTTP 200 con retardo 7 s → dispara `@Timeout` (6 s) | 1 | `escenario-latencia.json` |
| Comisión TAE | `servicio` empieza con `SERV-TAE` | 200 · 3.50 % · monto 17.50 | 2 | `comision-tae.json` |
| Comisión pago de servicios | `servicio` empieza con `SERV-PAGO` | 200 · 1.50 % · monto 7.50 | 2 | `comision-pago.json` |
| Comisión recarga | `producto` empieza con `PROD-RECARGA` | 200 · 2.00 % · monto 10.00 | 2 | `comision-recarga.json` |
| Comisión por defecto | cualquier otro caso válido | 200 · 2.50 % · monto 12.75 | 10 | `comisiones.json` |
| Token OAuth2 | `POST /oauth/token` | 200 · access_token mock (expires_in 300) | 5 | `oauth-token.json` |

> El disparador `transaccion` se llena desde el campo `transaccion` de la consulta del POS; `servicio`/`producto` igual. Ej.: para forzar timeout, manda `"transaccion": "TRX-SLOW-1"`.

### Ejemplos de prueba (body del POS al microservicio)

```jsonc
// Comisión TAE (3.5%)
{ "id":"POS-1","plaza":"PLZ-001","tienda":"TND-1","caja":"CAJA-1","transaccion":"TRX-1","servicio":"SERV-TAE-ATT" }
// Error 404
{ "id":"POS-2","plaza":"PLZ-001","tienda":"TND-1","caja":"CAJA-1","transaccion":"TRX-ERR-404","servicio":"SERV-X" }
// Timeout (7s)
{ "id":"POS-3","plaza":"PLZ-001","tienda":"TND-1","caja":"CAJA-1","transaccion":"TRX-SLOW","servicio":"SERV-X" }
```

## Administración de escenarios (CRUD)

Cada escenario es un stub manipulable vía la **admin API** de WireMock. El helper
`mock-admin.sh` la envuelve. Primero apunta `BASE` al mock:

- **Sandbox OpenShift:** `oc port-forward deploy/spin-mock 8082:8080` y luego `BASE=http://localhost:8082`.
- **Local (docker-compose):** ya está en `http://localhost:8082`.

```bash
# LISTAR (tabla de escenarios activos: id, método, url, http, prioridad)
BASE=http://localhost:8082 bash mock-admin.sh list

# LEER un escenario
bash mock-admin.sh get <id>

# CREAR un escenario (usa ejemplo-escenario.json como plantilla)
bash mock-admin.sh add ejemplo-escenario.json

# ACTUALIZAR un escenario existente
bash mock-admin.sh update <id> ejemplo-escenario.json

# BORRAR un escenario
bash mock-admin.sh delete <id>

# RESET: recargar todos los escenarios desde los archivos de mappings/
bash mock-admin.sh reset

# SAVE: persistir a archivos los escenarios creados/editados en caliente
bash mock-admin.sh save
```

Endpoints admin equivalentes (por si prefieres curl directo): `GET/POST/PUT/DELETE {BASE}/__admin/mappings[/{id}]`, `POST {BASE}/__admin/mappings/reset`, `POST {BASE}/__admin/mappings/save`.

## Aplicar cambios de archivos en el sandbox

Los stubs se montan desde el ConfigMap `spin-mock-mappings`. Tras editar/añadir
archivos en `mappings/`, recrea el ConfigMap y reinicia el mock:

```bash
oc create configmap spin-mock-mappings --from-file=mock-spin/mappings \
  --dry-run=client -o yaml | oc apply -f -
oc rollout restart deploy/spin-mock
```

> Nota: el `add/update/delete` por admin API es **en memoria** (no persiste al reiniciar el pod). Para cambios permanentes, edítalos como archivos en `mappings/` y aplica el ConfigMap, o usa `save` y copia los archivos generados al repo.
