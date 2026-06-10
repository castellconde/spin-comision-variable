# Spin Mock Service — simulador dirigido por base de datos

Microservicio (Quarkus, hexagonal) que **simula el API Spin**. En cada request
lee la tabla `escenario_spin` en PostgreSQL (misma BD `comvar` del piloto) y
arma la respuesta. Los escenarios se **administran con CRUD** (REST o SQL),
así que viven y se aplican en la base de datos.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/oauth/token` | Token OAuth2 mock (acepta cualquier credencial) |
| POST | `/v1/comisiones` | Respuesta de comisión resuelta por el escenario que aplique |
| GET | `/admin/escenarios` | Listar escenarios |
| GET | `/admin/escenarios/{id}` | Obtener uno |
| POST | `/admin/escenarios` | Crear |
| PUT | `/admin/escenarios/{id}` | Actualizar |
| DELETE | `/admin/escenarios/{id}` | Borrar |
| GET | `/swagger-ui`, `/openapi` | Documentación |
| GET | `/q/health/*` | Health checks |

## Tabla `escenario_spin`

| Columna | Uso |
|---|---|
| `nombre` | Descripción del escenario |
| `activo` | Si participa en la resolución |
| `prioridad` | Menor = se evalúa primero |
| `match_campo` | Campo a evaluar: `transaccion` \| `servicio` \| `producto` (o NULL = default) |
| `match_regex` | Regex Java; NULL = comodín (default) |
| `http_status` | Código HTTP a devolver |
| `porcentaje_comision`, `monto_comision`, `moneda`, `estatus` | Cuerpo de éxito (2xx) |
| `error_codigo`, `error_mensaje` | Cuerpo de error (no-2xx) |
| `delay_ms` | Retardo antes de responder (para timeouts) |

**Resolución:** se toma el primer escenario `activo` (orden `prioridad`, luego `id`)
cuyo `match_regex` haga *full match* sobre el valor de `match_campo`; si `match_regex`
es NULL, es el default (comodín). El seed inicial replica los escenarios previos
(errores 500/404/422, latencia `SLOW`, comisiones TAE/PAGO/RECARGA y default).

## Build y despliegue en el sandbox (reemplaza al WireMock)

```bash
# 1) Construir la imagen del mock desde GitHub (contexto = subcarpeta del módulo)
oc new-build --strategy=docker --name=spin-mock-service \
  --context-dir=spin-mock-service \
  https://github.com/castellconde/spin-comision-variable.git
oc logs -f bc/spin-mock-service        # espera a que termine

# 2) Resolver el ImageStream por nombre y desplegar (mantiene el Service 'spin-mock')
oc set image-lookup spin-mock-service
oc apply -f deploy/sandbox/32-spin-mock-db.yaml
oc rollout status deploy/spin-mock
```

> Reutiliza el Postgres `postgres` ya desplegado (BD `comvar`). El mock corre su
> propio Flyway con tabla de historial aislada (`flyway_history_spinmock`), así
> que NO choca con las migraciones del servicio principal.

## Administrar escenarios

### Por REST (CRUD)

```bash
# Apuntar al mock (port-forward o Route)
oc port-forward deploy/spin-mock 8082:8080

# Listar
curl -s http://localhost:8082/admin/escenarios | jq .

# Crear un escenario nuevo
curl -s -X POST http://localhost:8082/admin/escenarios -H "Content-Type: application/json" -d '{
  "nombre":"Comision premium","activo":true,"prioridad":2,
  "matchCampo":"servicio","matchRegex":"^SERV-PREMIUM.*",
  "httpStatus":200,"porcentajeComision":5.00,"montoComision":25.00,"moneda":"MXN","estatus":"CALCULADA","delayMs":0
}'

# Actualizar / borrar
curl -s -X PUT    http://localhost:8082/admin/escenarios/9 -H "Content-Type: application/json" -d '{ ... }'
curl -s -X DELETE http://localhost:8082/admin/escenarios/9
```

### Por SQL (misma BD del piloto)

```sql
SELECT id, nombre, prioridad, match_campo, match_regex, http_status FROM escenario_spin ORDER BY prioridad, id;
UPDATE escenario_spin SET porcentaje_comision = 4.0 WHERE nombre = 'Comision TAE';
INSERT INTO escenario_spin (nombre, prioridad, match_campo, match_regex, http_status, porcentaje_comision, monto_comision, moneda, estatus)
  VALUES ('Otra', 2, 'servicio', '^SERV-OTRA.*', 200, 2.75, 13.75, 'MXN', 'CALCULADA');
```

Los cambios aplican de inmediato (se leen en cada request). A diferencia del
mock WireMock anterior, **persisten** porque viven en PostgreSQL.

## Local (docker-compose)

El `docker-compose.yml` del repo sigue usando WireMock para arranque local rápido.
Para correr este mock dirigido por BD en local, constrúyelo (`./mvnw quarkus:dev`
en `spin-mock-service/` apuntando al Postgres del compose) o agrégalo como servicio
con `build: ./spin-mock-service`.
