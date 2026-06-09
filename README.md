# SPIN — Comisión Variable (piloto)

Microservicio de consulta de **comisión variable** desde punto de venta. Expone
un endpoint en la nube que consume el API del proveedor **Spin**, manteniendo
trazabilidad, seguridad y resiliencia.

- **Quarkus 3.33 LTS** · **Java 21** · empaquetado **nativo (Mandrel)**
- Imagen runtime: `registry.access.redhat.com/ubi10/ubi-minimal`
- Despliegue objetivo: **OpenShift ROSA / ARO**
- Desarrollo con **dev-containers** · **sin Lombok**

---

## Qué incluye

| Requisito | Implementación |
|---|---|
| Contratos dummy | `api/dto/ComisionRequest`, `ComisionResponse`, `ErrorResponse` |
| Validación de request (estilo *receiver* Oxxocel) | Bean Validation en el DTO + `ExceptionMappers` |
| Bitácora en PostgreSQL | Tabla `bitacora_consumo` **particionada por mes** (request + response + resultado + latencia) |
| Control de errores + reintentos implícitos (estilo *Transactions* Oxxocel) | `SpinClientService` con `@Retry` |
| Circuit breaker + rate limit configurables | `@CircuitBreaker`, `@RateLimit` (SmallRye), sobre-escribibles por env |
| Keycloak realm de aplicaciones | `quarkus-oidc`, realm `applications`, client `comision-variable-api` |
| Logs para Datadog | `quarkus-logging-json` + `TraceLoggingFilter` (trace_id / span_id / correlation_id) |
| Conjur por variables de ambiente | Todos los secretos vía `${VAR}` (ver `deploy/openshift/conjur-notes.md`) |
| Postman + Swagger | `../postman/` · Swagger UI en `/swagger-ui` |
| Health checks | `/q/health/live`, `/q/health/ready`, `/q/health/started` |
| Redis opcional | `TokenCacheService` con degradación elegante |

---

## Arranque rápido (piloto local)

Hay **dos formas equivalentes** de levantar el entorno. Elige una.

### Opción A — Dev-container (recomendada, alternativa a docker compose)

Requisitos: VS Code + extensión *Dev Containers* (o cualquier IDE compatible) y Docker/Podman.

1. Abre la carpeta `comision-variable` y ejecuta **"Reopen in Container"**.
2. El dev-container (`.devcontainer/docker-compose.yml`) levanta automáticamente
   el workspace **junto con** Postgres + Keycloak + Redis + mock Spin. No necesitas
   correr `docker compose` por separado.
3. Dentro del contenedor:

```bash
./mvnw quarkus:dev          # el perfil 'demo' ya viene en QUARKUS_PROFILE
```

Los servicios se resuelven por nombre (`postgres`, `keycloak`, `spin-mock`, `redis`).

### Opción B — docker compose manual

Requisitos: Docker / Podman, JDK 21, Maven (o el wrapper `./mvnw`).

```bash
# 1) Levantar dependencias (Postgres + Keycloak + Redis + mock Spin)
cd "SPIN Comision Variable"
docker compose up -d

# 2) Correr el microservicio en modo dev (perfil demo)
cd comision-variable
./mvnw quarkus:dev -Dquarkus.profile=demo
```

- API:        http://localhost:8080/rest/v1/comision-variable/comision
- Swagger UI:  http://localhost:8080/swagger-ui
- OpenAPI:     http://localhost:8080/rest/openapi
- Health:      http://localhost:8080/q/health
- Keycloak:    http://localhost:8081  (admin / admin)
- Mock Spin:   http://localhost:8082

### Probar con Postman
Importar `postman/SPIN-Comision-Variable.postman_collection.json` y el environment
`Comision Variable - Local`. Ejecutar en orden (el paso 1 guarda el token).

### Levantar TODO en contenedores (incluye el microservicio)
```bash
cd comision-variable && ./mvnw package          # genera fast-jar
cd .. && docker compose --profile full up -d --build
```

---

## Build nativo (empaquetado elegido)

```bash
cd comision-variable
./mvnw package -Dnative                          # usa builder Mandrel en contenedor
docker build -f src/main/docker/Dockerfile.native -t comision-variable:native .
```

Build JVM (alternativo, UBI10-minimal + Java 21):
```bash
./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t comision-variable:jvm .
```

---

## Resiliencia configurable (sin recompilar)

Todos estos parámetros se ajustan por **variable de ambiente** (ConfigMap en
OpenShift). Ejemplos:

| Variable | Default | Efecto |
|---|---|---|
| `SPIN_RETRY_MAX` | 3 | Reintentos hacia Spin |
| `SPIN_CB_FAILURE_RATIO` | 0.5 | Umbral de apertura del circuito |
| `SPIN_CB_DELAY_MS` | 5000 | Tiempo del circuito abierto |
| `SPIN_TIMEOUT_MS` | 6000 | Timeout por llamada |
| `SPIN_RATELIMIT_VALUE` | 50 | Peticiones por ventana |
| `SPIN_RATELIMIT_WINDOW_MS` | 1000 | Tamaño de ventana del rate limit |

Para probar la resiliencia: enviar `"transaccion": "FORCE-ERROR"` → el mock
responde 500 y se observan los reintentos y, tras varios fallos, la apertura del
circuito.

---

## Despliegue en OpenShift (ROSA / ARO)

```bash
oc new-project spin
oc apply -k deploy/openshift/
```

Los secretos del `secret-example.yaml` deben sustituirse por **CyberArk Conjur
Secrets Provider** (ver `deploy/openshift/conjur-notes.md`).

---

## Arquitectura (hexagonal — puertos y adaptadores)

**Requisito del proyecto:** este y todos los microservicios siguen arquitectura
hexagonal. El dominio es puro (sin frameworks); los adaptadores dependen del
dominio mediante puertos, nunca al revés.

```
src/main/java/com/oxxo/spin/comisionvariable/
├── domain/                         NÚCLEO — sin Quarkus/Jakarta/Jackson
│   ├── model/                      Consulta, Comision, RegistroConsumo, ResultadoConsulta
│   ├── port/in/                    ConsultarComisionUseCase        (puerto de entrada)
│   ├── port/out/                   ComisionProviderPort, BitacoraPort (puertos de salida)
│   └── exception/                  BusinessException, ComisionProviderException
├── application/                    ComisionVariableService implements ConsultarComisionUseCase
│                                   (orquesta puertos; solo depende del dominio)
├── adapter/
│   ├── in/rest/                    Resource + dto + mapper + exception   (driving)
│   └── out/
│       ├── spin/                   SpinComisionAdapter implements ComisionProviderPort
│       │                           (resiliencia + OAuth2 + mapper + fallback) (driven)
│       └── persistence/            BitacoraJpaAdapter implements BitacoraPort (driven)
└── infrastructure/                 config (OpenAPI), logging (trazas Datadog), health
```

Flujo de dependencias: `REST → ConsultarComisionUseCase ← Service → ComisionProviderPort / BitacoraPort ← Adaptadores`.
La tecnología de resiliencia (SmallRye FT) y de integración (Spin, JPA) queda
confinada a los adaptadores; el núcleo se prueba con JUnit puro
(`application/ComisionVariableServiceTest`).

## Estructura del repositorio

```
SPIN Comision Variable/
├── comision-variable/         # microservicio Quarkus (ver árbol hexagonal arriba)
│   ├── src/main/resources/    # application.properties + migraciones Flyway
│   ├── src/main/docker/       # Dockerfile.native / Dockerfile.jvm
│   └── .devcontainer/
├── deploy/openshift/          # manifiestos ROSA/ARO + notas Conjur
├── deploy/keycloak/           # realm 'applications'
├── mock-spin/                 # WireMock del API Spin
├── postman/                   # colección + environment
└── docker-compose.yml         # stack local completo
```
"# spin-comision-variable" 
