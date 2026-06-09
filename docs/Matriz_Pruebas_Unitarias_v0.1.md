# MATRIZ DE PRUEBAS UNITARIAS

## Control de Versiones

| No. de Versión | Fecha de cambio | Autor | Comentarios |
|---|---|---|---|
| 0.1 | 08/06/2026 | Roberto Castillo | Matriz inicial: 45 casos sobre la base de código hexagonal del piloto. |

## 1. Información general

| Campo | Valor |
|---|---|
| Proyecto | Piloto SPIN — Comisión Variable |
| Versión del código probada | 1.0.0-SNAPSHOT |
| Framework | JUnit 5 + Mockito + RestAssured |
| Coverage tool | JaCoCo |
| Coverage objetivo | ≥ 90% líneas e instrucciones (decisión JUN-002) |

## 2. Matriz de casos de prueba

| ID HU | HU | Componente probado | Caso de prueba | Tipo | Resultado esperado | Pasa/Falla | Notas |
|---|---|---|---|---|---|---|---|
| HU-001 | Consulta comisión | Consulta | tieneProductoOServicio falso si ambos vacíos | Unitario | retorna false | PASA | borde |
| HU-001 | Consulta comisión | Consulta | tieneProductoOServicio con producto o servicio | Unitario | retorna true | PASA | positivo |
| HU-001 | Consulta comisión | Consulta | concepto prioriza servicio sobre producto | Unitario | servicio gana | PASA | |
| HU-001 | Consulta comisión | RegistroConsumo | factory exito (200, OK) | Unitario | resultado OK | PASA | |
| HU-001 | Consulta comisión | RegistroConsumo | factory fallo | Unitario | resultado de error | PASA | negativo |
| HU-001 | Consulta comisión | BusinessException | expone código y status | Unitario | getters correctos | PASA | |
| HU-001 | Consulta comisión | ComisionProviderException | expone metadatos y causa | Unitario | getters correctos | PASA | |
| HU-001 | Consulta comisión | ComisionVariableService | sin producto ni servicio | Unitario | BusinessException 422 | PASA | negativo |
| HU-001 | Consulta comisión | ComisionVariableService | éxito registra bitácora OK | Unitario | devuelve comisión + bitácora OK | PASA | positivo |
| HU-001 | Consulta comisión | ComisionVariableService | fallo de proveedor | Unitario | registra fallo y relanza | PASA | negativo |
| HU-001 | Consulta comisión | ComisionRestMapper | toDomain copia campos | Unitario | mapeo correcto | PASA | |
| HU-001 | Consulta comisión | ComisionRestMapper | toResponse copia campos | Unitario | mapeo correcto | PASA | |
| HU-001 | Consulta comisión | SpinMapper | toSpin mapea referencia | Unitario | mapeo correcto | PASA | |
| HU-001 | Consulta comisión | SpinMapper | toDomain combina consulta+respuesta | Unitario | mapeo correcto | PASA | |
| HU-001 | Consulta comisión | SpinFallbackHandler | circuito abierto | Unitario | 503 CIRCUIT_OPEN | PASA | resiliencia |
| HU-001 | Consulta comisión | SpinFallbackHandler | rate limit | Unitario | 429 RATE_LIMITED | PASA | resiliencia |
| HU-001 | Consulta comisión | SpinFallbackHandler | timeout | Unitario | 504 TIMEOUT | PASA | resiliencia |
| HU-001 | Consulta comisión | SpinFallbackHandler | excepción de dominio | Unitario | se preserva | PASA | |
| HU-001 | Consulta comisión | SpinFallbackHandler | otra falla | Unitario | 502 ERROR | PASA | |
| HU-001 | Consulta comisión | SpinComisionAdapter | éxito mapea respuesta | Unitario | Comisión válida | PASA | positivo |
| HU-001 | Consulta comisión | SpinComisionAdapter | HTTP 401 | Unitario | invalida token, excepción | PASA | negativo |
| HU-001 | Consulta comisión | SpinComisionAdapter | HTTP 500 | Unitario | se traduce a 502 | PASA | negativo |
| HU-001 | Consulta comisión | SpinAuthService | usa cache si existe | Unitario | no llama auth | PASA | |
| HU-001 | Consulta comisión | SpinAuthService | renueva y cachea | Unitario | put en cache | PASA | |
| HU-001 | Consulta comisión | SpinAuthService | fallo de renovación | Unitario | ComisionProviderException 502 | PASA | negativo |
| HU-001 | Consulta comisión | TokenCacheService | get vacío inicial | Unitario | Optional vacío | PASA | |
| HU-001 | Consulta comisión | TokenCacheService | put y get vigente | Unitario | devuelve token | PASA | |
| HU-001 | Consulta comisión | TokenCacheService | token expirado | Unitario | no se devuelve | PASA | borde |
| HU-001 | Consulta comisión | TokenCacheService | invalidate | Unitario | limpia token | PASA | |
| HU-001 | Consulta comisión | BitacoraJpaAdapter | mapea y persiste | Unitario | persist invocado | PASA | |
| HU-001 | Consulta comisión | BitacoraJpaAdapter | nunca propaga errores | Unitario | no lanza | PASA | negativo |
| HU-001 | Consulta comisión | PartitionMaintenanceJob | crear y purgar | Unitario | invoca ambas funciones | PASA | |
| HU-001 | Consulta comisión | PartitionMaintenanceJob | nunca propaga errores | Unitario | no lanza | PASA | negativo |
| HU-001 | Consulta comisión | PartitionMaintenanceJob | skip predicate | Unitario | respeta flag | PASA | |
| HU-001 | Consulta comisión | ExceptionMappers | mapBusiness | Unitario | status y código | PASA | |
| HU-001 | Consulta comisión | ExceptionMappers | mapProvider | Unitario | status del proveedor | PASA | |
| HU-001 | Consulta comisión | ExceptionMappers | mapGeneric | Unitario | 500 INTERNAL_ERROR | PASA | |
| HU-001 | Consulta comisión | ExceptionMappers | mapValidation | Unitario | 400 con detalle | PASA | |
| HU-001 | Consulta comisión | TraceContext | sin MDC | Unitario | n/a | PASA | borde |
| HU-001 | Consulta comisión | TraceContext | con MDC | Unitario | valores correctos | PASA | |
| HU-001 | Consulta comisión | LivenessCheck | liveness | Unitario | UP | PASA | |
| HU-001 | Consulta comisión | SpinReadinessCheck | readiness | Unitario | UP | PASA | |
| HU-001 | Consulta comisión | ComisionVariableResource | sin autenticación | Integración | 401 | PASA | @QuarkusTest |
| HU-001 | Consulta comisión | ComisionVariableResource | request inválido | Integración | 400 con detalle | PASA | @QuarkusTest |
| HU-001 | Consulta comisión | ComisionVariableResource | request válido | Integración | 200 + body | PASA | @QuarkusTest |

## 3. Cobertura por capa

| Capa | Coverage objetivo | Estado |
|---|---|---|
| Dominio (model/exception) | ≥ 90% | Cubierto por pruebas puras |
| Aplicación (use case) | ≥ 90% | Cubierto (3 casos) |
| Adaptador entrada (REST/mappers/exception) | ≥ 90% | Cubierto (unit + @QuarkusTest) |
| Adaptador salida Spin (adapter/auth/cache/fallback) | ≥ 90% | Cubierto |
| Adaptador salida persistencia | ≥ 90% | Cubierto |
| Infraestructura (logging/health) | ≥ 90% | Cubierto |

> Nota: el porcentaje exacto lo emite JaCoCo al correr `./mvnw verify` (gate ≥ 0.90). Clases sin lógica (DTOs, entidades, interfaces de config y REST client) están excluidas del cómputo.

## 4. HUs sin tests

Ninguna. HU-001 cuenta con casos positivos, negativos y de borde.

## 5. Tests de integración

`ComisionVariableResourceTest` (`@QuarkusTest`) cubre el endpoint extremo a extremo del adaptador REST: autenticación (401), validación (400) y camino feliz (200) con el caso de uso mockeado.

<!-- fin -->
