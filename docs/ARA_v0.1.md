# ANÁLISIS DE RIESGO APLICATIVO (ARA)

## Control de Versiones

| No. de Versión | Fecha de cambio | Autor | Comentarios |
|---|---|---|---|
| 0.1 | 08/06/2026 | Roberto Castillo | ARA inicial del piloto Comisión Variable. |

## 1. Información general

| Campo | Valor |
|---|---|
| Proyecto | Piloto SPIN — Comisión Variable |
| Clasificación de información manejada | Interna (transaccional; sin datos personales sensibles) |
| Tipo de usuarios | Internos (sistemas POS, service-to-service) |
| Criticidad del negocio | [PENDIENTE: confirmar — propuesta Media] |

## 2. Activos identificados

| Activo | Tipo | Valor para el negocio | Confidencialidad | Integridad | Disponibilidad |
|---|---|---|---|---|---|
| Token OAuth2 de Spin | Credencial | Alto | Alta | Alta | Alta |
| Secretos (BD, Keycloak, Spin, apikey) | Credencial | Alto | Alta | Alta | Alta |
| Token JWT del POS | Credencial | Alto | Alta | Alta | Alta |
| Bitácora de consumo | Datos | Medio | Media | Alta | Media |
| API REST de consulta | Servicio | Alto | Media | Alta | Alta |

## 3. Amenazas identificadas

| ID | Amenaza | Activo afectado | Probabilidad | Impacto | Riesgo | Mitigación |
|---|---|---|---|---|---|---|
| AR-01 | Acceso no autenticado al endpoint | API REST | Media | Alto | Alto | OIDC/Keycloak; `@Authenticated` en `/rest/v1/**`; validación de JWT (seguridad-checklist §Autenticación) |
| AR-02 | Fuga de secretos | Credenciales | Baja | Alto | Medio | Conjur + variables de ambiente; sin secretos en repo/imagen (codigo-java §Secretos) |
| AR-03 | Inyección / payload malicioso | API/BD | Media | Medio | Medio | Bean Validation allow-list (regex) + tipado fuerte; JPA parametrizado |
| AR-04 | Agotamiento por dependencia (Spin) | Disponibilidad | Media | Alto | Alto | Circuit breaker, retry+jitter, timeout, rate limit configurables |
| AR-05 | Exposición de datos en logs/errores | Datos | Baja | Medio | Bajo | `ErrorResponse` sin stack; logs JSON sin secretos; sólo traceId al cliente |
| AR-06 | Abuso / DoS del endpoint | Disponibilidad | Media | Medio | Medio | Rate limit hacia Spin; (infra) WAF/Gateway + HPA |
| AR-07 | Manipulación de la bitácora | Datos | Baja | Medio | Bajo | Credenciales Conjur; mínimo privilegio; particiones |

## 4. Controles existentes

- [x] Autenticación con Keycloak (realm `applications`); rutas protegidas con `@Authenticated`.
- [x] Secretos vía Conjur (variables de ambiente); no hardcoded; `.gitignore`.
- [x] Validación de entrada con Bean Validation (`@Pattern`, `@Size`, `@NotBlank`).
- [x] Manejo centralizado de errores (`ExceptionMappers`) sin filtrar detalles internos.
- [x] Resiliencia: circuit breaker + retry + timeout + rate limit.
- [x] Trazabilidad: bitácora + logs JSON con trace_id/correlation_id (Datadog).
- [x] Contenedor sin privilegios (runAsNonRoot, drop ALL capabilities).
- [x] Cobertura de pruebas ≥ 90% (JaCoCo).
- [ ] WAF / API Gateway perimetral (a nivel infra — verificar con CMS).
- [ ] mTLS interno (a nivel infra).

## 5. Riesgos residuales aceptados

| Riesgo | Nivel residual | Justificación |
|---|---|---|
| AR-04 | Bajo-Medio | La resiliencia mitiga; el residual depende del SLA de Spin. |
| AR-06 | Medio | El rate limit aplica al consumo de Spin; la protección perimetral del endpoint queda pendiente de infraestructura. |

## 6. Plan de tratamiento de riesgos altos

| Riesgo | Acción | Responsable | Fecha objetivo |
|---|---|---|---|
| AR-01 | Validar configuración de issuer/audience en staging | [PENDIENTE] | [PENDIENTE] |
| AR-04 | Acordar SLA y umbrales de circuit breaker con el proveedor Spin | [PENDIENTE] | [PENDIENTE] |
| AR-06 | Habilitar WAF/rate limit perimetral en OpenShift Route/Gateway | [PENDIENTE] | [PENDIENTE] |

<!-- fin -->
