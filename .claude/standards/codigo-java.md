# Código Java — Reglas compactas para portales OXXO/FEMSA

> Consolida **STTI Estándar de Codificación Segura Java v2** (Ene 2023) + **Documento de Estándares 5.0 §6.3.4** (2021, partes timeless).
>
> Las secciones (§) preservan la numeración del estándar original para que los agentes citen así: *"viola STTI Java v2 §4.1"*.
>
> Stack target: Java 17 + Spring Boot 3.5+. Algunos defaults vienen de `.claude/project-config.yml`.

---

## Organización del código (Doc Estándares 5.0 §6.3.4)

### Paquete base obligatorio
```
com.femsa.oxxo.<aplicacion>
```
- `<aplicacion>` = `project.short_name` de `project-config.yml`.
- Variantes válidas para otros frentes FEMSA: `com.femsa.bara`, `com.femsa.farmacias`, `com.femsa.specialtys`, `com.femsa.immex`. Para EDT Comercial: siempre `com.femsa.oxxo`.

### Subpaquetes obligatorios
```
com.femsa.oxxo.<app>.controller/        # @RestController
com.femsa.oxxo.<app>.controller.soap/   # SOAP (si aplica)
com.femsa.oxxo.<app>.service/           # @Service (interfaces I*)
com.femsa.oxxo.<app>.service.implement/ # implementaciones
com.femsa.oxxo.<app>.service.dao/       # JDBC nativo / stored procs
com.femsa.oxxo.<app>.repository/        # Spring Data JPA
com.femsa.oxxo.<app>.entity/            # @Entity JPA
com.femsa.oxxo.<app>.dto/               # DTOs request/response
com.femsa.oxxo.<app>.model/             # POJOs de dominio
com.femsa.oxxo.<app>.config/            # @Configuration
com.femsa.oxxo.<app>.util/              # Interceptors, filters, LogSanitizer
com.femsa.oxxo.<app>.constant/          # Mensajes, ResourceMapping
com.femsa.oxxo.<app>.enums/             # Enumeraciones
com.femsa.oxxo.<app>.exeption/          # Excepciones custom (typo histórico — para legacy MDM-style)
com.femsa.oxxo.<app>.wsServices/        # Web service clients
```
- Para proyectos NUEVOS sin compatibilidad legacy MDM: usar `exception/` (sin typo).
- Para mantenimiento de proyectos MDM-style: conservar `exeption/` por consistencia.

### Reglas estructurales
- Un archivo = una clase/interfaz **pública**.
- Máximo **1000 líneas por archivo** — si crece más, refactorizar.
- Clase pública debe ir primero en el archivo.
- Clases privadas asociadas pueden ir en el mismo archivo solo si están funcionalmente vinculadas.

### Header obligatorio en cada archivo
```java
/*
 * @(#)<NombreClase>.java <version> <DD/MM/YY>
 *
 * Copyright <YYYY> FEMSA Comercio, OXXO. All rights reserved.
 * OXXO PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/**
 * <Descripción de qué hace esta clase>
 *
 * @author <nombre>
 * @version <X.Y>, <DD/MM/YY>
 * @since <java-version>
 */
```

### Orden de imports
1. `java.*` y `javax.*` del JDK.
2. Frameworks y opensource (`org.springframework.*`, `org.apache.*`, `lombok.*`, etc.).
3. Componentes propios de FEMSA Comercio / OXXO.

---

## STTI Java v2 — Reglas de seguridad

### §1. Fundamentos
**§1.1 Mantener código simple.** Lógica compleja esconde bugs de seguridad. Refactorizar `if/else` anidados > 3 niveles.

**§1.2 Evitar la duplicación.** Código duplicado lleva a parchear unos lugares y olvidar otros. Extraer a util/service común.

**§1.3 Restringir privilegios.** Cada componente con el mínimo acceso que necesita. Especialmente para conexiones DB: el usuario de aplicación NUNCA debe ser DBA.

**§1.4 Minimizar verificaciones de permisos.** Centralizar en interceptors / filters, no esparcir `if (user.hasRole(...))` por todos lados.

**§1.5 Encapsular.** Campos `private`, getters/setters cuando aplique. NUNCA `public` para mutable state.

**§1.6 Código seguro de terceros.** Verificar dependencias con CVEs conocidos antes de agregarlas. Ejecutar `mvn dependency-check:check` periódicamente.

**§1.7 Documentar información de seguridad.** Cada método sensible (auth, crypto, file I/O) con javadoc que mencione asunciones de seguridad.

**§1.8 Denegación de servicio.** Defensivamente limitar: tamaño de uploads, profundidad de recursión, número de elementos en colecciones del usuario.

### §2. Información confidencial

**§2.1 Quitar información sensible de excepciones.**
🔴 NO propagar excepciones técnicas al cliente. Mapear a códigos HTTP genéricos:

| Excepción interna | Código HTTP devuelto |
|---|---|
| `BadRequestException` | 400 |
| `UnauthorizedException` | 401 |
| `ForbiddenException` | 403 |
| `NotFoundException` | 404 |
| `ConflictException` | 409 |
| `InternalServerErrorException` | 500 |
| `ServiceUnavailableException` | 503 |

Implementar en `@RestControllerAdvice` global. No exponer mensajes de Hibernate, paths internos, ni stacktraces.

**§2.2 No registrar información altamente confidencial.**
🔴 Prohibido en logs: tarjetas bancarias, credenciales, PII (RFC, CURP, email completo). Si necesitas trazar un usuario, usar ID enmascarado.

❌ MAL:
```java
log.severe("Error al registrar el cliente: " + clienteNuevo.toString());
log.error("Token recibido: " + token);
```

✅ BIEN:
```java
log.error("Error al registrar el cliente ID={}", LogSanitizer.sanitize(clienteId));
log.debug("Token recibido (prefix): {}***", token.substring(0, 6));
```

**§2.3 Quitar información sensible de la memoria.** Para datos altamente confidenciales (contraseñas en memoria), usar `char[]` en lugar de `String`, llenar con ceros después de uso, NO esperar al GC.

**§2.4 No codificar información confidencial.** Secretos vía `${VAR}` desde Azure Key Vault. Detección automática: regex `[A-Za-z0-9_-]{32,}` en `application*.properties` → flag.

### §3. Inyección e inclusión

**§3.1 SQL Injection.**
🔴 NUNCA concatenar input del usuario en queries.

❌ MAL:
```java
@Query(value = "SELECT * FROM users WHERE email = '" + userInput + "'", nativeQuery = true)
```

✅ BIEN:
```java
@Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
List<User> findByEmail(@Param("email") String email);
```

**§3.2 Command Injection.**
🔴 NUNCA construir comandos shell con concatenación.

❌ MAL: `Runtime.getRuntime().exec("ls " + userInput);`
✅ BIEN: usar `ProcessBuilder` con argumentos separados + lista blanca de comandos.

**§3.3 LDAP Injection / XPath / etc.** Mismos principios: parametrizar, no concatenar.

**§3.4 Tener cuidado al interpretar código no confiable.** Evitar `ScriptEngine`, `eval`, deserialización de fuentes no confiables.

### §4. Mutabilidad

**§4.1 Preferir la inmutabilidad para tipos de valor.** Usar `record` (Java 17) para DTOs. Campos `final` cuando aplique.

**§4.2 Crear copias defensivas de valores de salida mutables.** Si un getter retorna una `List`, retornar `List.copyOf(internalList)`.

**§4.3 No exponer colecciones modificables.** Usar `Collections.unmodifiableList()` o `List.copyOf()`.

**§4.4 Tratar entrada/salida de objetos no confiables como sospechosos.** Validar siempre, no asumir tipos.

### §5. Validación de entradas

Usar `jakarta.validation` (Bean Validation 3.0):

```java
public class UsuarioCreateDto {
    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    private String username;

    @NotNull
    @Email
    @Size(max = 255)
    private String email;

    @NotNull
    @Min(18) @Max(120)
    private Integer edad;
}

@PostMapping("/usuarios")
public ResponseEntity<UsuarioDto> crear(@Valid @RequestBody UsuarioCreateDto dto) { ... }
```

En path variables:
```java
@GetMapping("/usuarios/{id}")
public ResponseEntity<UsuarioDto> obtener(
    @PathVariable @Pattern(regexp = "^\\d+$") @Size(max = 10) String id
) { ... }
```

### §6. Autenticación y autorización
Ver `CLAUDE.md §5 — Patrón Access Control`. El portal NO valida JWT localmente; delega al `ValidateTokenInterceptor`.

### §7. Manejo de recursos

🔴 SIEMPRE `try-with-resources` para `Connection`, `Statement`, `ResultSet`, `InputStream`, `OutputStream`:

```java
try (Connection conn = dataSource.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {
    // ...
}
```

**HikariCP configuration mínima:**
```properties
spring.datasource.<name>.hikari.maximum-pool-size=20
spring.datasource.<name>.hikari.connection-timeout=30000
spring.datasource.<name>.hikari.max-lifetime=1800000
spring.datasource.<name>.hikari.leak-detection-threshold=60000
# Oracle: spring.datasource.<name>.hikari.connection-test-query=SELECT 1 FROM DUAL
# Postgres: (default OK, no test-query needed)
```

**RestTemplate / WebClient con timeouts obligatorios:**
```java
@Bean
public RestTemplate restTemplate() {
    return new RestTemplateBuilder()
        .setConnectTimeout(Duration.ofSeconds(5))
        .setReadTimeout(Duration.ofSeconds(30))
        .build();
}
```

> Referencia al caso real: `WEB_ReingenieriaAccessControl` (Abr 2026) — connection leak en `try-with-resources` mal armado consumió 2 conexiones por SSO sin liberar, agotó pool de 40 con ~20 sesiones concurrentes. Fix: declarar `Connection` como recurso explícito en el `try`, + activar `setLeakDetectionThreshold(30000)`.

### §8. Logging seguro (CWE-117 Log Forging)

🔴 Todo input externo debe pasar por `LogSanitizer.sanitize()` antes de loggearse.

```java
// util/LogSanitizer.java
public final class LogSanitizer {
    private LogSanitizer() {}
    public static String sanitize(String input) {
        if (input == null) return null;
        // Remueve CR/LF, controles, limita longitud
        return input.replaceAll("[\\r\\n\\t]", "_")
                    .replaceAll("[\\x00-\\x1F]", "")
                    .substring(0, Math.min(input.length(), 500));
    }
}
```

Niveles correctos:
- `INFO` — flujo normal de negocio
- `WARN` — condiciones recuperables (retry, fallback)
- `ERROR` — fallos reales, con stacktrace pero NUNCA PII
- `DEBUG` — diagnóstico, desactivar en producción

### §9. Manejo de errores (configuración Spring)

```properties
server.error.include-message=never
server.error.include-binding-errors=never
server.error.include-stacktrace=never
server.error.include-exception=false
```

`@RestControllerAdvice` global obligatorio:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidation(...) { /* 400 */ }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFound(...) { /* 404 */ }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGeneric(...) {
        log.error("Error interno", e);
        return ResponseEntity.status(500).body(new ErrorDto("Error interno"));
        // NO devolver e.getMessage() — STTI Java v2 §2.1
    }
}
```

---

## Convenciones de versionado (Doc Estándares 5.0 §6.3.2)

Formato: `WEB_<NombreProyecto>-<X.Y.Z>`
- `X` (major): rediseño / cambio mayor.
- `Y` (minor): control de cambios / mejora menor.
- `Z` (patch): bugfix.

Primera versión en producción: `1.0.0`.

Conventional Commits:
```
feat: agregar endpoint POST /usuarios
fix: corregir leak en SecurityService.decryptSession (CHO 0039135)
chore: actualizar versión de Spring Boot a 3.5.4
docs: actualizar Esp Técnica con cambios del CHG0039834
refactor: extraer LogSanitizer a util/
test: agregar tests para AdministracionRolService
```

Mensaje de commit (legacy SVN, ahora Git):
```
<Nombre> <DD/MM/YY>: <Mensaje descriptivo>
Ejemplo: Hugo Méndez 12/09/23: Fix CHO 56789644 corrección de mensaje desplegado en pantalla
```

---

## Referencias cruzadas
- `seguridad-checklist.md` — OWASP Top 10 unificado
- `codigo-plsql.md` — si stack.database = oracle
- `codigo-shell.md` — si el proyecto incluye jobs Shell de Control-M
