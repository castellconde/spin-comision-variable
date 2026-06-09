# ESPECIFICACIÓN TÉCNICA
## Sistema de Trabajo TI

## Identificación del Proyecto

| Campo | Valor |
|---|---|
| Proyecto | Piloto SPIN — Comisión Variable |
| Plataforma / Satélite | Quarkus 3.33 LTS (Java 21) sobre OpenShift ROSA/ARO; imagen UBI10-minimal |
| Procesos de negocio involucrados | Consulta de comisión variable desde punto de venta |
| Responsables núcleo | [PENDIENTE: Gerente de Proyecto / Líder TI núcleo] |
| Líder de Diseño | [PENDIENTE: nombre / correo] |
| EDA(s) autores | [PENDIENTE] |
| Arquitecto(s) aplicativo validador(es) | [PENDIENTE] |
| Líder Técnico | Roberto Castillo <roberto.castillo@oxxo.com> |
| Desarrollador(es) | [PENDIENTE] |

## Control de Versiones

| No. de Versión | Fecha de cambio | Autor | Comentarios |
|---|---|---|---|
| 0.1 | 08/06/2026 | Roberto Castillo | Versión inicial del piloto: arquitectura hexagonal, integración Spin, bitácora particionada, seguridad Keycloak, observabilidad Datadog, secretos Conjur. |

## 1. Presentación del Producto

### 1.1 Objetivo

Implementar una arquitectura de referencia para proyectos en la nube basada en microservicios. Se requiere una implementación limpia, segura y confiable para la consulta de comisiones desde servicios OnPremise expuestos en Cloud, evaluando las ventajas de Quarkus frente a otros frameworks.

### 1.2 Alcance

Consultar comisiones variables desde el punto de venta con datos de entrada por plaza, tienda, caja, transacción y producto y/o servicio. Servicio en la nube (AWS ROSA) que expone un endpoint y a su vez consume un API del proveedor Spin, manteniendo la trazabilidad de la consulta. Incluye seguridad con Keycloak (realm de aplicaciones), bitácora en PostgreSQL, integración con Datadog por agente e integración con Conjur por agente.

### 1.3 Sistemas Involucrados

| Sistema | Rol | Modo |
|---|---|---|
| Punto de Venta (POS) | Consumidor del API | demo / production |
| Keycloak (realm `applications`) | Autenticación service-to-service (OIDC/OAuth2) | demo (local/docker) / production |
| API Spin | Proveedor del cálculo de comisión (OAuth2 apikey+token) | sandbox (demo) / production |
| PostgreSQL 18 | Bitácora y configuración | demo / production |
| Redis (opcional) | Cache de token; degradación elegante si no está | demo / production |
| Datadog | Logs y trazas (por agente) | production |
| CyberArk Conjur | Gestión de secretos (variables de ambiente) | production |

### 1.4 No Contempla

No contempla servicios de front-end.

### 1.5 Estrategia de despliegue

Despliegue en OpenShift ROSA/ARO (namespace `spin`) vía manifiestos en `deploy/openshift/` (kustomize). Build de imagen nativa (Mandrel) o JVM (UBI10-minimal + Java 21). [PENDIENTE: integración con pipelines CMS / Azure DevOps].

### 1.6 Información de ejecución del producto

Job de mantenimiento de la bitácora particionada:

| Parámetro | Valor |
|---|---|
| Modo de ejecución | Quarkus Scheduler (`@Scheduled`, cron configurable) |
| Script de ejecución | `PartitionMaintenanceJob.ejecutar()` |
| Ventana de tiempo de ejecución | Diaria, `0 30 2 * * ?` (configurable) |
| Frecuencia de ejecución | Diaria |
| Dependencias | Acceso a PostgreSQL (funciones `crear_particiones_futuras` / `purgar_particiones_antiguas`) |
| Volumen | Según volumen de consultas registradas |
| Tiempo de ejecución | Segundos |
| Tipo | No-Crítico (Operación no detenida) |
| Máximo tiempo fuera de servicio | [PENDIENTE: SLA] |

### 1.7 Definiciones, Acrónimos y Abreviaciones

| Abreviación | Descripción |
|---|---|
| POS | Punto de Venta |
| OIDC | OpenID Connect |
| CB | Circuit Breaker |
| MDC | Mapped Diagnostic Context (contexto de logs) |
| ROSA / ARO | Red Hat OpenShift on AWS / Azure Red Hat OpenShift |
| Conjur | CyberArk Conjur (gestión de secretos) |

## 2. Modelo Arquitectura

### 2.1 Diagrama de Arquitectura

[PENDIENTE: Figura 01 — diagrama de arquitectura]. Estilo: **hexagonal (puertos y adaptadores)**. Flujo: `REST → ConsultarComisionUseCase ← Service → ComisionProviderPort / BitacoraPort ← Adaptadores (Spin, PostgreSQL)`.

### 2.2 Referencias de Estándares Usados

| Nombre de documento | Ruta del estándar |
|---|---|
| Estándar de Codificación Segura Java | `.claude/standards/codigo-java.md` |
| Checklist de Seguridad | `.claude/standards/seguridad-checklist.md` |
| Entregables de Portal | `.claude/standards/entregables-portal.md` |

### 2.3 Especificaciones de Hardware y Software

| Elemento | Versión |
|---|---|
| Java | 21 LTS |
| Quarkus | 3.33 LTS |
| Imagen runtime | `registry.access.redhat.com/ubi10/ubi-minimal` |
| Base de datos | PostgreSQL 18 |
| Migraciones | Flyway |
| Orquestación | OpenShift ROSA / ARO |

### 2.4 Seguridad

Autenticación service-to-service con Keycloak (realm `applications`); todas las rutas `/rest/v1/**` exigen Bearer JWT. Secretos inyectados por variable de ambiente desde CyberArk Conjur (no se versionan ni se hornean en la imagen). Validación estricta de entrada (allow-list por regex). Manejo de errores homogéneo sin filtrar detalles internos. Ver ARA y MDA.

### 2.5 Componentes a reutilizar

| Nombre de componente | Objetivo de reutilizarlo |
|---|---|
| `TraceLoggingFilter` | Trazabilidad (trace_id/span_id/correlation_id) correlacionable con Datadog |
| `SpinFallbackHandler` | Traducción de fallas técnicas a excepciones de dominio |
| Funciones SQL de particiones | Mantenimiento de la bitácora (crear/purgar) |

## 3. Requerimientos

### 3.1 Manejo de Errores y Excepciones

#### 3.1.1 Roles y usuarios

| Rol | Descripción |
|---|---|
| Equipo de Soporte L2 | [PENDIENTE: buzón de soporte del proyecto] |
| Operador en turno | [PENDIENTE] |

#### 3.1.2 Errores, advertencias y avisos

| ID | Descripción | Acción | Rol Receptor |
|---|---|---|---|
| 1 | `VALIDATION_ERROR` (400) — request inválido | Corregir payload según detalle por campo | Consumidor (POS) |
| 2 | `PRODUCTO_O_SERVICIO_REQUERIDO` (422) — regla de negocio | Enviar al menos producto o servicio | Consumidor (POS) |
| 3 | `SPIN_*` (502/503/504/429) — falla del proveedor | Reintentar; si persiste, escalar a soporte con traceId | Soporte L2 |
| 4 | `INTERNAL_ERROR` (500) — error no controlado | Escalar con traceId | Soporte L2 |

### 3.2 Entrada y Salida

`POST /rest/v1/comision-variable/comision`

Request (JSON): `id`, `plaza`, `tienda`, `caja`, `transaccion` (obligatorios), `producto` y/o `servicio`.
Response 200 (JSON): `id`, `plaza`, `tienda`, `caja`, `concepto`, `porcentajeComision`, `montoComision`, `moneda`, `timestamp` + header `Location`.
Errores: `ErrorResponse` con `codigo`, `mensaje`, `status`, `traceId`, `timestamp`, `errores[]`.

### 3.3 Reglas de Transformación

Mapeo en adaptadores: `ComisionRequest` (DTO) → `Consulta` (dominio) → `SpinComisionRequest`; `SpinComisionResponse` → `Comision` (dominio) → `ComisionResponse` (DTO). El concepto prioriza servicio sobre producto.

## 4. Diseño Técnico

### 4.1 Modelado de la aplicación

#### 4.1.1 Diagrama de despliegue

[PENDIENTE: Figura 02 — diagrama de despliegue OpenShift]

#### 4.1.2 Diagrama de componentes

[PENDIENTE: Figura 03 — diagrama de componentes]. Componentes principales: adaptador REST (entrada), servicio de aplicación (caso de uso), adaptador Spin (resiliencia + OAuth2), adaptador de persistencia (bitácora), infraestructura (logging/health/OpenAPI).

#### 4.1.3 Diagrama Entidad-Relación

[PENDIENTE: Figura 04 — diagrama ER]. Dos tablas independientes:

```
bitacora_consumo (PARTICIONADA POR MES, RANGE creado_en)
  id BIGINT IDENTITY · correlation_id · trace_id · consulta_id · plaza · tienda
  caja · transaccion · request_payload JSONB · response_payload JSONB
  http_status · resultado · error_codigo · error_mensaje · intentos
  latencia_ms · creado_en TIMESTAMPTZ      PK (id, creado_en)

configuracion_servicio
  clave VARCHAR(80) PK · valor · descripcion · actualizado_en
```

#### 4.1.4 Diagrama de secuencia

[PENDIENTE: Figura 05 — secuencia de consulta de comisión]

### 4.2 Cambios de la versión actual

Versión inicial (v0.1). No aplica comparación con versión previa.

### 4.3 Restricciones de diseño

Microservicios en Quarkus (último LTS) sobre UBI10-minimal + Java 21; despliegue en OpenShift ROSA/ARO; uso de dev-containers; sin Lombok; arquitectura hexagonal obligatoria (decisión JUN-002); cobertura de pruebas ≥ 90% (JUN-002).

### 4.4 Requerimientos de Licencia

Dependencias open source bajo Apache 2.0 / EPL (Quarkus, SmallRye, Hibernate, Flyway). Imagen base UBI10 (Red Hat, términos UBI). [PENDIENTE: validación formal de licencias por el área correspondiente].

### 4.5 Componentes Comprados

NA.

### 4.6 Requerimientos de base de datos

| Base de datos | Esquema | Tabla / Vista | Permisos |
|---|---|---|---|
| comvar (PostgreSQL) | public | `bitacora_consumo` | SELECT, INSERT |
| comvar (PostgreSQL) | public | `configuracion_servicio` | SELECT |
| comvar (PostgreSQL) | public | funciones `crear_particiones_futuras`, `purgar_particiones_antiguas` | EXECUTE |

### 4.7 Especificaciones del Software

NA (servicio backend; sin componente POS en este piloto).

## Firmas de aprobación

| Patrocinador |
|---|
| Firma: |
| Nombre: [PENDIENTE] |
| Puesto: [PENDIENTE] |
| Área: [PENDIENTE] |
| Fecha: |

| Líder de Iniciativa |
|---|
| Firma: |
| Nombre: [PENDIENTE] |
| Fecha: |

| Líder de Proyecto TI |
|---|
| Firma: |
| Nombre: [PENDIENTE] |
| Fecha: |

<!-- fin -->
