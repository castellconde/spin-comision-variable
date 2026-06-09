# Checklist de Seguridad Unificado — OWASP Top 10 + STTI OXXO/FEMSA

> Punto único de entrada cuando un agente quiere validar un tema de seguridad transversal. Cruza **OWASP Top 10 2021** con los estándares OXXO específicos (STTI Java v2, STTI Angular v2, STTI PL/SQL v2) y con experiencia interna del equipo.
>
> Cita: *"viola seguridad-checklist §A03 — SQL Injection"*.

---

## A01:2021 — Broken Access Control

**Síntomas:** usuario X puede ver/modificar datos del usuario Y. Endpoint admin accesible sin rol admin. Path traversal (`../`) que escapa del directorio esperado.

**Validaciones obligatorias:**
- ✅ Toda ruta no-pública pasa por `ValidateTokenInterceptor` (mode=production).
- ✅ Authorization check **en el backend** además del guard de Angular.
- ✅ Path variables validadas con `@Pattern` y `@Size`.
- ✅ Ningún endpoint expone IDs internos que permitan IDOR (Insecure Direct Object Reference) — usar UUIDs o validar ownership.

**Referencias:**
- CLAUDE.md §5 — Patrón Access Control
- `codigo-java.md` §6, `codigo-angular.md` §7.4

---

## A02:2021 — Cryptographic Failures

**Síntomas:** datos sensibles en texto plano. Algoritmos débiles (MD5, SHA1, DES). Llaves hardcoded.

**Validaciones obligatorias:**
- ✅ HTTPS forzado (HSTS con maxAge ≥ 1 año).
- ✅ Secretos solo desde Azure Key Vault vía `${VAR}`.
- ✅ Passwords con `BCrypt`/`Argon2`, NUNCA MD5/SHA1.
- ✅ Si se almacena PII: cifrado at-rest (lo gestiona infra OXXO).
- ✅ TLS 1.2+ obligatorio.

**Referencias:**
- `codigo-java.md` §2.4 — No codificar info confidencial
- CLAUDE.md §3.3 — Manejo de secretos

---

## A03:2021 — Injection

**Síntomas:** SQL injection, command injection, LDAP injection, XSS (también está en A03 oficialmente).

**Validaciones obligatorias:**

### SQL
- ✅ Solo `@Param` en JPQL nativo o `:paramName` en queries.
- ✅ En PL/SQL: `EXECUTE IMMEDIATE ... USING :1` (bind variables).
- ✅ NUNCA concatenar input del usuario en strings de query.

### XSS
- ✅ Angular sanitiza por default — no usar `bypassSecurityTrust*` con input del usuario.
- ✅ Header `Content-Security-Policy` configurado en backend.
- ✅ Header `X-Content-Type-Options: nosniff`.

### Command Injection
- ✅ Usar `ProcessBuilder` con argumentos separados, no `Runtime.exec(String)`.
- ✅ Validar contra lista blanca si el comando viene del usuario.

**Referencias:**
- `codigo-java.md` §3 — Inyección e inclusión
- `codigo-angular.md` §3 — Prevención XSS
- `codigo-plsql.md` §3 — Texto dinámico seguro

---

## A04:2021 — Insecure Design

**Síntomas:** flujos diseñados sin pensar en abuso. Lógica de negocio que asume "el usuario es honesto".

**Validaciones obligatorias:**
- ✅ Threat modeling completado antes de iniciar desarrollo (ver MDA en `entregables-portal.md`).
- ✅ Análisis de Riesgo Aplicativo (ARA) firmado por Seguridad TI.
- ✅ Rate limiting en endpoints sensibles (verificar con CMS/F5).
- ✅ Validación adversarial: "¿cómo lo rompería un atacante autenticado?".

**Referencias:**
- `entregables-portal.md` — MDA + ARA obligatorios

---

## A05:2021 — Security Misconfiguration

**Síntomas:** stacktraces en respuestas. CORS abierto (`*`). Actuator endpoints expuestos. Swagger en producción. Errores de configuración en headers de seguridad.

**Validaciones obligatorias:**
- ✅ `server.error.include-stacktrace=never` en prod
- ✅ `server.error.include-message=never` en prod
- ✅ CORS específico, no `*`. CORS list en `application*.properties`.
- ✅ Actuator: solo `health` e `info` expuestos. NUNCA `env`, `heapdump`, `loggers`, `mappings`, `beans`, `caches` en prod.
- ✅ `springdoc.swagger-ui.enabled=false` en prod (salvo justificación documentada).
- ✅ HSTS configurado.
- ✅ X-Frame-Options: DENY (o SAMEORIGIN según necesidad).
- ✅ Dependencias actualizadas — `mvn dependency-check:check` periódico.

**Referencias:**
- `codigo-java.md` §9 — Manejo de errores
- CLAUDE.md §3.4 — Reglas generales

---

## A06:2021 — Vulnerable and Outdated Components

**Síntomas:** dependencias con CVEs conocidos sin parchear.

**Validaciones obligatorias:**
- ✅ Backend: `mvn dependency-check:check` — bloquear merge si hay `high`/`critical`.
- ✅ Frontend: `npm audit --production` limpio. `ng update` periódico.
- ✅ Checkmarx (corporativo, CMS) en cada PR a `staging`.
- ✅ Snyk o equivalente para complemento.

**Referencias:**
- `codigo-angular.md` §15 — Escaneo de componentes vulnerables
- `codigo-java.md` §1.6 — Código seguro de terceros

---

## A07:2021 — Identification and Authentication Failures

**Síntomas:** auth débil, sesiones que no expiran, tokens en localStorage, "olvidé mi contraseña" inseguro.

**Validaciones obligatorias:**
- ✅ Auth siempre delegada a AC. El portal NO implementa auth.
- ✅ JWT validado por AC en cada request (`/validateToken`).
- ✅ Tokens en `sessionStorage`, NO `localStorage` (XSS protection).
- ✅ Logout invalida la sesión en AC.
- ✅ Timeout de sesión configurado (ver con GlobalSoft).

**Referencias:**
- `codigo-angular.md` §8 — JWT
- `codigo-angular.md` §9 — sessionStorage
- CLAUDE.md §5 — Patrón AC

---

## A08:2021 — Software and Data Integrity Failures

**Síntomas:** deserialización insegura, pipelines que aceptan código sin firma, dependencias de fuentes no confiables.

**Validaciones obligatorias:**
- ✅ NO usar `ObjectInputStream` con datos no confiables.
- ✅ Si se usa serialización, preferir formatos con esquema (Protocol Buffers, Avro) o JSON con validación estricta.
- ✅ Maven Central + Anthropic-aprobados como únicos repositorios.
- ✅ Pipelines CI/CD firmados (lo gestiona CMS).

**Referencias:**
- `codigo-java.md` §4 — Mutabilidad y serialización

---

## A09:2021 — Security Logging and Monitoring Failures

**Síntomas:** ataques que pasan inadvertidos porque no hay logs. Logs sin contexto. PII en logs.

**Validaciones obligatorias:**
- ✅ Eventos críticos loggeados: login, cambios de permisos, accesos a datos sensibles, errores 500.
- ✅ `LogSanitizer.sanitize()` para todo input externo (CWE-117).
- ✅ NUNCA loggear PII, credenciales, tokens completos.
- ✅ Logs centralizados (lo gestiona infra OXXO).
- ✅ Alertas configuradas para patrones sospechosos (lo gestiona Seguridad TI).

**Referencias:**
- `codigo-java.md` §2.2, §8 — Logging seguro
- `codigo-plsql.md` §4.2 — Manejo de excepciones

---

## A10:2021 — Server-Side Request Forgery (SSRF)

**Síntomas:** endpoint que recibe URL del usuario y la sigue, atacante hace que el backend acceda a recursos internos.

**Validaciones obligatorias:**
- ✅ URLs que vienen del usuario se validan contra lista blanca antes de seguirlas.
- ✅ Si el portal hace `RestTemplate.exchange(userProvidedUrl, ...)`, ALARMA — refactorizar.
- ✅ `RestTemplate` con timeouts cortos (no infinitos) — evita hangs por SSRF a recursos lentos.

**Referencias:**
- `codigo-java.md` §7 — Manejo de recursos
- `codigo-angular.md` §11 — Redirecciones

---

## Casos específicos del equipo EDT Comercial

### Caso 1: Connection Leak en AC (Abr 2026)

**Bug:** en `WEB_ReingenieriaAccessControl`, `try-with-resources` mal armado dejaba `Connection` fuera del bloque try (se abría inline dentro de `prepareCall()`), causando que cada SSO consumiera 2 conexiones que nunca volvían al pool de 40. Con ~20 sesiones concurrentes, agotaba el pool y todos los portales caían.

**Fix:** declarar `Connection` como recurso explícito en el `try`, + activar `setLeakDetectionThreshold(30000)` en `BdVault.java`.

**Aplicable a TODO portal:** revisar siempre que `Connection`, `Statement` y `ResultSet` estén en el `try-with-resources` (no inline en `prepareCall`/`prepareStatement`). Ver `codigo-java.md §7`.

### Caso 2: CWE-117 Log Forging en MDM

**Bug histórico:** input del usuario loggeado sin sanitizar permitía inyectar saltos de línea + entradas falsas en logs.

**Fix:** `LogSanitizer.sanitize()` aplicado a todo input externo antes de loggear.

**Aplicable a TODO portal:** el `code-reviewer` debe verificar que `log.*({input})` siempre pase por `LogSanitizer`. Ver `codigo-java.md §8`.

---

## Severidad según modo del proyecto

| Hallazgo | mode=demo | mode=production |
|---|---|---|
| Secretos hardcoded | 🔴 | 🔴 |
| SQL Injection | 🔴 | 🔴 |
| XSS via bypassSecurityTrust | 🔴 | 🔴 |
| Connection leak (recursos no cerrados) | 🔴 | 🔴 |
| Logging sin sanitizar (CWE-117) | 🟡 | 🟡 |
| Ausencia de `ValidateTokenInterceptor` | 🟡 | 🔴 |
| CORS abierto `*` | 🟡 | 🔴 |
| Stacktrace en respuesta | 🟡 | 🔴 |
| Token en `localStorage` | 🟡 | 🟡 |
| Falta de tests para path crítico | 🟡 | 🟡 |

---

## Referencias cruzadas
- `codigo-java.md` — backend
- `codigo-angular.md` — frontend
- `codigo-plsql.md` — BD Oracle (si aplica)
- `entregables-portal.md` — ARA, MDA, Principios Seguridad (S-SDLC)
- CLAUDE.md §5 — Patrón Access Control
