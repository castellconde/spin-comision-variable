# 00 — Índice de Estándares OXXO/FEMSA

> Esta carpeta consolida los estándares oficiales OXXO/FEMSA en forma compacta y citable. Los agentes de Claude Code los consultan al validar código y citan referencias específicas en sus reportes.
>
> Los documentos originales (PDFs, DOCX corporativos) viven en `_originales/` para consulta detallada. **El LT los descarga de SharePoint** — no se versionan en Git por confidencialidad.

---

## Mapa de estándares

| Pregunta del LT | Va a este archivo | Cita en agentes como |
|---|---|---|
| ¿Cómo escribir Java correctamente? | [`codigo-java.md`](codigo-java.md) | `STTI Java v2 §X` o `Doc Estándares 5.0 §6.3.4` |
| ¿Cómo evitar XSS/CSRF en Angular? | [`codigo-angular.md`](codigo-angular.md) | `STTI Angular v2 §X` |
| ¿Cómo escribir PL/SQL seguro? | [`codigo-plsql.md`](codigo-plsql.md) | `STTI PL/SQL v2 §X` |
| ¿Cómo escribir Shell scripts seguros? | [`codigo-shell.md`](codigo-shell.md) | `Doc Estándares 5.0 §6.3.5` |
| ¿Qué cubre OWASP Top 10 para mi stack? | [`seguridad-checklist.md`](seguridad-checklist.md) | Compendio de los anteriores |
| ¿Qué color uso para el botón primario? | [`diseno-tokens.yml`](diseno-tokens.yml) | `Sistema de Diseño Portales Web §2.1` |
| ¿Cómo se ve el componente X según diseño? | [`diseno-componentes.md`](diseno-componentes.md) | `Sistema de Diseño Portales Web §5.X` |
| ¿Qué documentos debo entregar al cerrar el proyecto? | [`entregables-portal.md`](entregables-portal.md) | `WEB Training Material v3 §6` |

---

## Documentos fuente originales

Estos son los archivos corporativos completos. Vienen de SharePoint corporativo y NO se versionan en Git. El LT los descarga al clonar el repo:

| Archivo | Vigencia | Contenido |
|---|---|---|
| `Documento_de_estándares_5_0.docx` | 2021 (parcialmente vigente) | Foundational. Java + Shell + Oracle + Seguridad + Versionamiento. **~30% desactualizado** (SVN, Java 1.6/1.8, WebLogic 11g/12g). Conservar para reglas atemporales (naming, headers, organización). |
| `STTI_Estandar_de_Codificacion_Segura_-_JAVA_v2.docx` | Ene 2023 (vigente) | Codificación segura Java. OWASP, inyección, validación, secretos. **Referencia activa.** |
| `STTI_Estándar_de_Codificacion_Segura_-_PL_SQL_v2.docx` | Ene 2023 (vigente) | Codificación segura PL/SQL. SQL injection, cursores, privilegios. Aplica si stack.database = oracle. |
| `STTI_Estándar_de_Codificación_Segura__Angular_v2.docx` | Ene 2023 (vigente) | Codificación segura Angular. XSS, CSRF, JWT, OWASP Top 10, sessionStorage, escape contextual. **Crítico** para code-reviewer frontend. |
| `EstandaresDiseño-PortalesWebOXXO.pdf` | 2024+ (vigente) | Sistema de diseño. Paleta FEMSA + OXXO, Open Sans, grid 12 col / 1366px, Boxicons + Flaticon, components specced. |
| `WEB_-_Training_Material.pptx` | V3 (vigente) | Stack tecnológico oficial, entregables obligatorios por proyecto, estructura SharePoint, servidores DEV, contactos L2/L3. |

---

## Cómo los agentes citan reglas

Cuando un agente reporta un hallazgo, **siempre** incluye referencia a la regla violada con el formato:

```
Viola <Estándar> <§sección> — <descripción corta>
Ver: .claude/standards/<archivo>.md sección <X.Y>
```

Ejemplo real:

> 🔴 **Blocker:** Viola STTI Angular v2 §3.4 — uso de `DomSanitizer.bypassSecurityTrustHtml()` con input del usuario (línea 47 de `producto-detalle.component.ts`).
>
> Ver: `.claude/standards/codigo-angular.md` sección 3.4

Esto hace que el LT pueda ir al estándar consolidado para entender el porqué, y si necesita más detalle, al original en SharePoint.

---

## Mantenimiento de los estándares consolidados

- **Trimestralmente:** el EDT revisa si hay versiones nuevas de los STTI o del Doc Estándares en SharePoint. Si las hay, regenera los `.md` consolidados.
- **Cuando un proyecto descubre una regla nueva** que vale para todos (ej. una vulnerabilidad encontrada en code review): se propone vía PR al template y se documenta en el `.md` correspondiente.
- **NO duplicar contenido sin necesidad:** si una regla aplica a Java Y PL/SQL (ej. "no concatenar input en queries"), va una vez en `seguridad-checklist.md` y se referencia desde los otros.

---

## Última sincronización

| Estándar | Versión vigente en SharePoint | Versión reflejada aquí | Fecha de última sincronización |
|---|---|---|---|
| Doc Estándares | 5.0 | 5.0 (parcial — solo timeless) | 2026-05-19 |
| STTI Java | v2 (Ene 2023) | v2 | 2026-05-19 |
| STTI Angular | v2 (Ene 2023) | v2 | 2026-05-19 |
| STTI PL/SQL | v2 (Ene 2023) | v2 | 2026-05-19 |
| EstandaresDiseño PDF | 1.0 (Ene 2024) | 1.0 | 2026-05-19 |
| WEB Training Material | V3 | V3 | 2026-05-19 |
