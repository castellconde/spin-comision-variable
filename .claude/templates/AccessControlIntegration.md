# Integración con Access Control — Snippets listos para copiar

> Código probado, extraído de `WEB_PortalMDM_V2_back`. Cópialo y adapta nombres a tu proyecto.
>
> **Ubicación correcta:** este archivo vive en `.claude/templates/` (NO en `.claude/agents/`). En v1.0 estaba mal ubicado; en v2.0 se movió aquí para que Claude Code lo reconozca como template y no intente cargarlo como sub-agente.

---

## 1. Backend — Spring Boot 3.5+

### 1.1 Dependencias en `pom.xml`

```xml
<!-- JWT (solo para parsing, NO para validación local) -->
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>java-jwt</artifactId>
    <version>4.4.0</version>
</dependency>

<!-- Logging Log4j2 (excluir Logback) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>
```

### 1.2 `ValidateTokenInterceptor.java` (production)

```java
package com.femsa.oxxo.<aplicacion>.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
@Profile("!local-demo")
public class ValidateTokenInterceptor implements HandlerInterceptor {

    private static final Logger log = LogManager.getLogger(ValidateTokenInterceptor.class);

    @Value("${endpoint.validate.token}")
    private String endpointValidateToken;

    @Value("${endpoint.validate.appId}")
    private String expectedAppId;

    private final RestTemplate restTemplate;

    public ValidateTokenInterceptor(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("token");
        String appId = request.getHeader("appId");

        if (token == null || appId == null) {
            log.warn("Request sin headers token/appId: {}", LogSanitizer.sanitize(request.getRequestURI()));
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        if (!expectedAppId.equals(appId)) {
            log.warn("appId no coincide con esperado: {}", LogSanitizer.sanitize(appId));
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        try {
            Map<String, String> body = new HashMap<>();
            body.put("token", token);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> ac = restTemplate.exchange(
                    endpointValidateToken, HttpMethod.POST, entity, Map.class);

            Boolean valid = (Boolean) ac.getBody().get("valid");
            if (Boolean.TRUE.equals(valid)) {
                return true;
            }
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;

        } catch (Exception e) {
            log.error("Error validando token contra AC", e);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;  // fail-closed: si AC está caído, NO permitir el request
        }
    }
}
```

### 1.3 `MockValidateTokenInterceptor.java` (demo)

```java
package com.femsa.oxxo.<aplicacion>.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Profile("local-demo")
public class MockValidateTokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return true;  // siempre acepta — solo para demo
    }
}
```

### 1.4 `AppConfig.java` — registrar el interceptor activo

```java
@Configuration
public class AppConfig implements WebMvcConfigurer {

    private final List<HandlerInterceptor> interceptors;

    public AppConfig(ObjectProvider<HandlerInterceptor> providers) {
        this.interceptors = providers.stream().toList();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        interceptors.stream()
            .filter(i -> i.getClass().getSimpleName().contains("ValidateToken"))
            .forEach(i -> registry.addInterceptor(i)
                .addPathPatterns("/rest/**")
                .excludePathPatterns(
                    "/actuator/**",
                    "/rest/v1/portal-<short>/public/**"
                ));
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }
}
```

### 1.5 `application.properties` — variables obligatorias

```properties
# Resueltas en pipeline desde Azure Key Vault
endpoint.validate.token=${AC_URL_BACK}/validateToken
endpoint.validate.user=${AC_URL_BACK}/api/user
endpoint.validate.appId=${<NOMBRE>_APPID}
```

### 1.6 `SecurityConfig.java` — CORS

```java
@Configuration
public class SecurityConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/rest/**")
                .allowedOrigins(allowedOrigins.split(","))  // de application.properties, NO "*"
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "token", "appId", "Authorization")
                .maxAge(3600);
    }
}
```

---

## 2. Frontend — Angular 19

### 2.1 HTTP interceptor para headers obligatorios

```typescript
// app/interceptors/token.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { environment } from '../../environments/environment';

export const tokenInterceptor: HttpInterceptorFn = (req, next) => {
    const auth = inject(AuthService);
    const token = auth.getToken();

    if (!token) {
        // sin token, dejamos pasar (login no requiere token)
        return next(req);
    }

    const cloned = req.clone({
        setHeaders: {
            token,
            appId: environment.appId
        }
    });
    return next(cloned);
};
```

Registrar en `app.config.ts`:
```typescript
providers: [
    provideHttpClient(withInterceptors([tokenInterceptor]))
]
```

### 2.2 `environment.ts`

```typescript
// environment.demo.ts
export const environment = {
    production: false,
    apiBase: '/rest/v1/portal-<short>',
    appId: 'PORTAL_<NOMBRE>'  // valor del project-config.yml en modo demo
};

// environment.prod.ts
export const environment = {
    production: true,
    apiBase: '/rest/v1/portal-<short>',
    appId: '<placeholder reemplazado en build CI/CD con valor de Key Vault>'
};
```

### 2.3 AuthGuard

```typescript
// app/guards/auth.guard.ts
import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (auth.isAuthenticated()) return true;

    router.navigate(['/login']);
    return false;
};
```

### 2.4 Manejo del token — sessionStorage (NO localStorage)

```typescript
// app/services/auth.service.ts
@Injectable({ providedIn: 'root' })
export class AuthService {
    private readonly TOKEN_KEY = 'portal-token';

    setToken(token: string) {
        // STTI Angular v2 §9.1: sessionStorage, NO localStorage
        sessionStorage.setItem(this.TOKEN_KEY, token);
    }

    getToken(): string | null {
        return sessionStorage.getItem(this.TOKEN_KEY);
    }

    clearToken() {
        sessionStorage.removeItem(this.TOKEN_KEY);
    }

    isAuthenticated(): boolean {
        return !!this.getToken();
    }
}
```

---

## 3. Lo que NO debes hacer

- ❌ NO implementar JWT validation local con `spring-security-oauth2-resource-server`. AC lo hace.
- ❌ NO hacer calls directos a Auth0 desde el backend del portal.
- ❌ NO cachear el resultado de `validateToken` localmente más allá del request actual.
- ❌ NO bypassear el interceptor en endpoints "internos" que reciben datos del usuario.
- ❌ NO guardar el token en `localStorage` — usar `sessionStorage`.
- ❌ NO hardcodear el `appId` — debe venir de `${VAR}` en production.

---

## 4. Adaptación para tu proyecto

Reemplaza estos placeholders:

| Placeholder | Reemplazo |
|---|---|
| `<aplicacion>` | `project.short_name` de project-config.yml (ej. `categorias`, `mdm`) |
| `<short>` | igual que arriba, para `/rest/v1/portal-<short>` |
| `<NOMBRE>` | nombre del proyecto en MAYÚSCULAS para variable Key Vault (ej. `CATEGORIAS_APPID`) |

---

## Referencias
- CLAUDE.md §5 — Patrón Access Control
- `.claude/standards/codigo-java.md` §6 — Auth Java
- `.claude/standards/codigo-angular.md` §8 — JWT + headers
- `WEB_PortalMDM_V2_back` — implementación de referencia (repo oficial)
