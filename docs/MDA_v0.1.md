# MODELADO DE AMENAZAS (MDA)
> Acompañar este documento con su contraparte .tm7 en Microsoft Threat Modeling Tool.

## Control de Versiones

| No. de Versión | Fecha de cambio | Autor | Comentarios |
|---|---|---|---|
| 0.1 | 08/06/2026 | Roberto Castillo | Modelado STRIDE inicial del piloto. |

## 1. Información general

| Campo | Valor |
|---|---|
| Proyecto | Piloto SPIN — Comisión Variable |
| Clasificación de información | Interna (transaccional) |
| Criticidad | [PENDIENTE: propuesta Media] |

## 2. Diagrama de Flujo de Datos (DFD)

[PENDIENTE: Diagrama nivel 0 — contextual]
[PENDIENTE: Diagrama nivel 1 — componentes principales]

Descripción textual del flujo:

1. POS → comision-variable vía `POST /rest/v1/comision-variable/comision` (HTTPS + Bearer JWT).
2. comision-variable → Keycloak (realm `applications`): validación del JWT.
3. comision-variable → API Spin: obtención de token OAuth2 (apikey) y consulta de comisión (HTTPS).
4. comision-variable → PostgreSQL: registro de bitácora.
5. comision-variable → Redis (opcional): cache de token.
6. comision-variable → Datadog: logs/trazas.
7. Conjur → variables de ambiente del contenedor (secretos).

Límites de confianza: TB-1 POS↔Route; TB-2 ↔Keycloak; TB-3 ↔API Spin; TB-4 ↔PostgreSQL/Redis.

## 3. Análisis STRIDE

### 3.1 Spoofing (suplantación)

| Amenaza | Componente | Mitigación |
|---|---|---|
| Suplantación del consumidor POS | API REST | JWT firmado por Keycloak; `@Authenticated` |
| Emisor de token falso | Keycloak/OIDC | Validación de firma e issuer en la configuración OIDC |

### 3.2 Tampering (manipulación)

| Amenaza | Componente | Mitigación |
|---|---|---|
| Alteración del payload en tránsito | Red (POS/Spin) | TLS (Route edge / HTTPS a Spin); validación de entrada |
| Modificación de la bitácora | PostgreSQL | Credenciales Conjur; mínimo privilegio |

### 3.3 Repudiation (no repudio)

| Amenaza | Componente | Mitigación |
|---|---|---|
| Negación de una consulta | Bitácora/Logs | Registro con `correlation_id`/`trace_id` y timestamp |

### 3.4 Information Disclosure (divulgación)

| Amenaza | Componente | Mitigación |
|---|---|---|
| Exposición de stacktrace | API | `ErrorResponse` homogéneo, sin stack al cliente |
| Secretos/credenciales en logs | Logs | Logs JSON sin secretos; secretos sólo por Conjur |

### 3.5 Denial of Service

| Amenaza | Componente | Mitigación |
|---|---|---|
| Saturación del proveedor Spin | Integración | Circuit breaker + rate limit + timeout |
| Pool de conexiones agotado | PostgreSQL | Tamaño de pool configurado; consultas acotadas |
| Flood de peticiones al endpoint | API | (infra) WAF/Gateway + HPA |

### 3.6 Elevation of Privilege

| Amenaza | Componente | Mitigación |
|---|---|---|
| Acceso a operaciones no permitidas | API | Roles del realm; servicio de solo consulta; mínimo privilegio |

## 4. Checklist para importar al .tm7

- [ ] External entities: POS, Keycloak, API Spin, Datadog, Conjur.
- [ ] Process: comision-variable.
- [ ] Data stores: PostgreSQL (bitácora/config), Redis (cache).
- [ ] Data flows: los 7 del DFD (sección 2) con su protocolo.
- [ ] Trust boundaries: TB-1..TB-4.
- [ ] Generar amenazas automáticas y validar contra la tabla STRIDE (sección 3).
- [ ] Documentar estado (mitigado / pendiente) y adjuntar el `.tm7` en SharePoint.

## 5. Amenazas no mitigadas (issues abiertos)

| Amenaza | Severidad | Acción |
|---|---|---|
| Flood al endpoint (sin WAF/Gateway perimetral) | Media | Habilitar protección perimetral en infraestructura |
| mTLS interno entre servicios | Baja | Evaluar cuando esté disponible en la malla/cluster |

<!-- fin -->
