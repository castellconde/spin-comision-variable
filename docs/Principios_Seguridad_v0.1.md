# PRINCIPIOS DE SEGURIDAD APLICADOS (S-SDLC)

## Control de Versiones

| No. de Versión | Fecha de cambio | Autor | Comentarios |
|---|---|---|---|
| 0.1 | 08/06/2026 | Roberto Castillo | Principios S-SDLC aplicados en el piloto. |

## 1. Información general

| Campo | Valor |
|---|---|
| Proyecto | Piloto SPIN — Comisión Variable |
| Versión del código | 1.0.0-SNAPSHOT |
| Fecha | 08/06/2026 |

## 2. Principios S-SDLC aplicados

### 2.1 Fase de Requisitos

- [x] Requisitos de seguridad documentados en `specs/04-decisiones.md` (JUN-002) y `specs/03-integraciones.md`.
- [x] Clasificación de datos identificada (ver ARA §1): información interna, sin datos personales sensibles.
- [ ] Stakeholders de seguridad consultados (Seguridad TI FEMSA) — [PENDIENTE].

**Evidencia:** `specs/`, decisión JUN-002.

### 2.2 Fase de Diseño

- [x] Modelado de amenazas completado (ver MDA).
- [x] Análisis de riesgo aplicativo completado (ver ARA).
- [x] Modelo de autenticación documentado (Keycloak realm `applications`, Esp. Técnica §2.4).
- [x] Manejo de secretos definido (CyberArk Conjur, variables de ambiente).
- [x] Arquitectura hexagonal que aísla el núcleo y facilita revisión/pruebas.

### 2.3 Fase de Codificación

- [x] Estándares seguidos (`.claude/standards/codigo-java.md`, `seguridad-checklist.md`).
- [x] Sin secretos hardcoded (validable con `security-auditor`).
- [x] Validación de entrada con allow-list (regex) y tipado fuerte.
- [x] Manejo de errores sin filtrar detalles internos; logs sin datos sensibles.
- [ ] Code review con agente `code-reviewer` en cada PR — [PENDIENTE: integrar en flujo].

**Evidencia:** código en `comision-variable/`, `ExceptionMappers`, `application.properties`.

### 2.4 Fase de Pruebas

- [x] Pruebas unitarias con gate de cobertura ≥ 90% (JaCoCo) — ver Matriz de Pruebas.
- [ ] Auditoría con agente `security-auditor` antes de PR a staging — [PENDIENTE].
- [ ] Análisis SAST (Checkmarx, gestionado por CMS) — [PENDIENTE].

**Evidencia:** suite de 45 casos; configuración JaCoCo en `pom.xml`.

### 2.5 Fase de Liberación

- [x] Runbook generado (ver Runbook §4–6).
- [ ] Variables de ambiente verificadas en Conjur — [PENDIENTE: ambiente].
- [ ] Monitoreo y alertas configuradas en Datadog — [PENDIENTE].
- [x] Plan de rollback documentado (Runbook §6.3).

### 2.6 Fase de Operación

- [ ] Revisión periódica de logs (semanal) — [PENDIENTE].
- [ ] Re-auditoría de seguridad post-liberación — [PENDIENTE].
- [ ] Actualización continua de dependencias con vulnerabilidades — [PENDIENTE].

## 3. Hallazgos de auditoría

[PENDIENTE: ejecutar `security-auditor`]. Sin hallazgos formales registrados en esta versión.

## 4. Acciones pendientes

| Acción | Severidad | Responsable | Fecha objetivo |
|---|---|---|---|
| Habilitar WAF/rate limit perimetral | 🟡 | [PENDIENTE] | [PENDIENTE] |
| Integrar SAST (Checkmarx) en pipeline | 🟡 | [PENDIENTE] | [PENDIENTE] |
| Configurar alertas en Datadog | 🟢 | [PENDIENTE] | [PENDIENTE] |

## 5. Firmas

| Rol | Nombre | Firma | Fecha |
|---|---|---|---|
| Líder Técnico | Roberto Castillo | | |
| Líder de Proyecto TI | [PENDIENTE] | | |
| Responsable de Seguridad TI | [PENDIENTE] | | |

<!-- fin -->
