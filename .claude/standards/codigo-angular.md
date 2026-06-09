# Código Angular — Reglas compactas para portales OXXO/FEMSA

> Consolida **STTI Estándar de Codificación Segura Angular v2** (Ene 2023).
>
> Secciones (§) preservan numeración del estándar original. Cita en agentes: *"viola STTI Angular v2 §3.4"*.
>
> Stack target: Angular 19+ (configurable en `.claude/project-config.yml` → `stack.frontend.version`).

---

## §1. Introducción
Cubre protecciones integradas de Angular contra vulnerabilidades comunes. NO cubre auth (lo cubre AC) ni autorización (lo cubre el guard).

## §2. Fundamentos

**§2.1 Actualizaciones.** Mantener Angular en versión soportada (LTS). Verificar con `ng update` y `npm audit` periódicamente.

**§2.2 No modificar la copia de Angular.** Nunca editar `node_modules/@angular/*`. Si necesitas alterar comportamiento, hacerlo vía componentes/services propios.

**§2.3 Establecer límites de confianza.** El frontend NUNCA es la fuente de verdad para validación — el backend SIEMPRE re-valida. El frontend valida UX, el backend valida seguridad.

**§2.4 Evitar APIs marcadas "Riesgo de seguridad".** Angular marca explícitamente algunas APIs como peligrosas (`bypassSecurityTrust*`). Solo usarlas cuando es absolutamente necesario, con sanitización manual.

---

## §3. Prevención de XSS (Cross-Site Scripting)

🔴 **CRÍTICO.** XSS es la #1 causa de pwn de portales web.

**§3.1 Modelo de seguridad XSS de Angular.** Por default, Angular trata TODOS los valores como NO confiables. Te protege automáticamente cuando usas `{{ }}` interpolation o `[property]` binding.

**§3.2 Contextos de saneamiento.** Angular reconoce 4 contextos de seguridad:
- `HTML` — interpolación en innerHTML
- `STYLE` — atributo style
- `URL` — atributos href, src
- `RESOURCE_URL` — script src, iframe src (más estricto)

**§3.3 Ejemplo de sanitización (auto).**
```typescript
// ✅ SEGURO — Angular sanitiza automáticamente
@Component({ template: `<div [innerHTML]="contenidoUsuario"></div>` })
```

**§3.4 Uso directo de APIs DOM y desinfección explícita.**
🔴 **NUNCA hacer esto sin sanitización:**

```typescript
// ❌ PELIGROSO — bypass del modelo de seguridad
this.html = this.sanitizer.bypassSecurityTrustHtml(userInput);

// ❌ PELIGROSO — manipulación directa del DOM
this.elementRef.nativeElement.innerHTML = userInput;
```

```typescript
// ✅ Si necesitas insertar HTML (caso raro): sanitizar primero
import { DomSanitizer, SecurityContext } from '@angular/platform-browser';

const clean = this.sanitizer.sanitize(SecurityContext.HTML, userInput);
this.html = clean;
```

**§3.5 Política de Seguridad del Contenido (CSP).** Configurar headers CSP en el backend:
```
Content-Security-Policy: default-src 'self'; script-src 'self'; object-src 'none';
                         style-src 'self' 'unsafe-inline'; frame-ancestors 'none';
```

**§3.6 Usar el compilador de plantillas offline (AOT).** Angular CLI lo hace por default desde v9. Verificar `"aot": true` en `angular.json`.

**§3.7 Protección XSS del lado del servidor.** Aún con Angular, el backend debe escapar HTML en respuestas y headers `X-Content-Type-Options: nosniff`.

---

## §4. Confiar en valores seguros

Cuando estás 100% seguro de que un valor es seguro (ej. config que viene del servidor en HTTPS, controlada por tu equipo), usar:
```typescript
this.url = this.sanitizer.bypassSecurityTrustResourceUrl(trustedUrl);
```

🔴 **Regla:** si el valor pudo haber pasado por input del usuario en CUALQUIER momento, NO es seguro. No uses `bypassSecurityTrust*`.

---

## §5. Vulnerabilidades a nivel HTTP

**§5.1 CSRF (Cross-Site Request Forgery).**
Spring REST + Angular: CSRF deshabilitado en el backend (es API REST). En su lugar, dependes de:
- Headers `token` + `appId` requeridos en cada request → AC valida.
- Mismo origen (CORS estricto).

Si por requerimiento debes usar CSRF tokens:
```typescript
// app.module.ts
import { HttpClientXsrfModule } from '@angular/common/http';

@NgModule({
  imports: [HttpClientXsrfModule.withOptions({
    cookieName: 'XSRF-TOKEN',
    headerName: 'X-XSRF-TOKEN',
  })]
})
```

**§5.2 XSSI (Cross-Site Script Inclusion).** Para JSON sensible, el backend debe prefijar la respuesta con `)]}',\n` (Angular lo strippea automáticamente). Spring no lo hace por default — agregar `MessageConverter` custom si aplica.

---

## §6. Auditoría de aplicaciones Angular

Ejecutar regularmente:
```bash
ng audit         # Angular-specific checks
npm audit        # CVEs en dependencias
npm audit fix    # auto-fix de bajo riesgo
```

En CI/CD: bloquear merge si hay vulnerabilidades `high` o `critical` en producción.

---

## §7. OWASP Top 10 para Angular

**§7.1 Dependencias con vulnerabilidades conocidas (A06:2021).** `npm audit --production` debe estar limpio.

**§7.2 Broken authentication (A07:2021).** No implementar auth en Angular; delegar a AC vía `validateToken`.

**§7.3 Cross-Site Scripting (A03:2021).** Ver §3 arriba.

**§7.4 Broken access control (A01:2021).** Guards en cada ruta sensible:
```typescript
const routes: Routes = [
  { path: 'admin', component: AdminComponent, canActivate: [AuthGuard, AdminRoleGuard] }
];
```
🔴 NUNCA confíes solo en el guard — el backend SIEMPRE re-valida la autorización.

**§7.5 Sensitive data exposure (A02:2021).** No mostrar PII completa en pantalla salvo necesidad. Enmascarar tarjetas, RFCs, etc.

---

## §8. Autenticación con JWT

**§8.1 JWT.** Token emitido por Auth0/AC, NUNCA generado por el portal.

**§8.2 Sesiones con JWT.** No persistir tokens más allá de la sesión.

**§8.3 Token del lado del servidor.** El backend del portal NO valida el JWT localmente — lo manda a AC vía `/validateToken`.

**§8.4 Uso del JWT del lado del cliente.**
```typescript
// http-interceptor.service.ts
intercept(req: HttpRequest<unknown>, next: HttpHandler) {
  const token = this.authService.getToken();
  const appId = environment.appId;
  const cloned = req.clone({
    setHeaders: { token, appId }
  });
  return next.handle(cloned);
}
```

**§8.5 Retorno del JWT al código del servidor.** Headers `token` y `appId` en cada request a `/rest/**`. Sin headers → 401.

---

## §9. Storage seguro

**§9.1 localStorage vs sessionStorage.**

| Storage | Persistencia | Recomendado para |
|---|---|---|
| `localStorage` | Persiste entre sesiones del navegador | ❌ NO para tokens — riesgo de XSS persistente |
| `sessionStorage` | Se limpia al cerrar pestaña | ✅ Tokens de sesión |
| Cookie `httpOnly + secure + sameSite=strict` | Server-side | ✅ Más seguro pero requiere cambio de arquitectura |

🔴 Default obligatorio: tokens en `sessionStorage`.

**§9.2 Origen, protocolo y subdominio.** Recordar que `sessionStorage` está aislado por origen — funciona para SPAs single-domain.

---

## §10. Cargando templates de forma segura

**§10.1 Solución.** Usar AOT compilation (default en Angular 9+). NUNCA cargar templates de URLs externas en runtime.

---

## §11. Redirecciones

**§11.1 Mitigar problemas de redirección.**
❌ MAL: `window.location = this.route.snapshot.queryParams['redirect'];`
✅ BIEN: validar contra una lista blanca de URLs internas antes de redirigir.

```typescript
const allowedRedirects = ['/dashboard', '/profile', '/orders'];
const redirect = this.route.snapshot.queryParams['redirect'];
if (allowedRedirects.includes(redirect)) {
  this.router.navigateByUrl(redirect);
} else {
  this.router.navigateByUrl('/dashboard');
}
```

---

## §12. Strict Contextual Escaping

**§12.1 Activar en `tsconfig.json`:**
```json
{
  "angularCompilerOptions": {
    "strictTemplates": true,
    "strictInjectionParameters": true,
    "strictInputAccessModifiers": true
  }
}
```

**§12.2 Evitar plantillas generadas dinámicamente.** NUNCA construir un template HTML con concatenación de strings que incluyan input del usuario.

---

## §13. Inyección de código Angular del lado del servidor

Si el backend genera HTML que contiene snippets de Angular (raro), tratar input del usuario como hostil — escapar `{{`, `}}`, `[(`, `)]`.

---

## §14. Linters de seguridad

Configurar ESLint con:
```bash
npm install --save-dev @angular-eslint/eslint-plugin @typescript-eslint/eslint-plugin eslint-plugin-security
```

`.eslintrc.json`:
```json
{
  "extends": [
    "@angular-eslint/recommended",
    "plugin:@typescript-eslint/recommended",
    "plugin:security/recommended"
  ]
}
```

---

## §15. Escaneo de componentes vulnerables

Snyk, OWASP Dependency-Check, o Checkmarx (corporativo, gestionado por CMS).
- En cada PR a `staging`: scan automático.
- Si hay vulnerabilidades `high`/`critical`: bloquear merge.

---

## §16. No usar APIs DOM nativas

🔴 Prohibido:
- `document.write`
- `element.innerHTML = userInput`
- `element.outerHTML = userInput`
- `eval`, `Function(...)`, `setTimeout(string, ...)`, `setInterval(string, ...)`

✅ Usar:
- `Renderer2` de Angular para manipulación de DOM
- `ViewChild` + property binding

---

## Convenciones del equipo

- **NgRx obligatorio** para estado compartido entre componentes — no `BehaviorSubject` global ni servicios singleton con state.
- **Lógica de negocio en services / effects** — los `*.component.ts` solo orquestan UI.
- **Subscripciones con cleanup**: `takeUntil(this.destroy$)`, `async` pipe, o `unsubscribe` en `ngOnDestroy`.
- **HTTP tipado**: `HttpClient.get<TypedResponse>()`, NUNCA `any`.
- **CatchError en pipes** que pueden fallar.
- **Tests con coverage > 80%**: componentes + services nuevos.

---

## Identidad visual (cuando aplica)

Ver `.claude/standards/diseno-tokens.yml` y `.claude/standards/diseno-componentes.md`. La paleta activa la define `.claude/project-config.yml → design.palette` (default: `oxxo`).

---

## Referencias cruzadas
- `seguridad-checklist.md` — OWASP Top 10 cruzado con backend
- `codigo-java.md` — backend pair
- `diseno-tokens.yml` — paleta + tipografía oficial
