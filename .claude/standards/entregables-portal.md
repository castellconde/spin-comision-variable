# Entregables Obligatorios — Proyecto de Portal EDT Comercial

> Lista oficial de entregables documentales que cada proyecto de portal debe producir y mantener, según **WEB Training Material v3** del onboarding OXXO/FEMSA.
>
> Cita: *"WEB Training Material v3 §6 — Entregables"*.

---

## Los 6 entregables documentales

Para cada uno, el agente `doc-writer` puede generar el draft inicial y actualizar incrementalmente. La conversión a `.docx` corporativo la hace `scripts/generate-docs.py`.

| # | Entregable | Plantilla oficial | Fase | Frecuencia de actualización | Generado por |
|---|---|---|---|---|---|
| 1 | **Especificación Técnica** | FCTI_CNF_Especificación_Técnica_Rev_5 | Diseño | En cada cambio de arquitectura | `doc-writer` |
| 2 | **Runbook** | FCTI_CNF_PGD_Runbook_Rev_2 | Pre-QA | En cada release que afecte operación | `doc-writer` |
| 3 | **Matriz de Pruebas Unitarias** | (formato simple, sin plantilla .dotx oficial) | Construcción | Con cada HU nueva | `doc-writer` + LT |
| 4 | **Análisis de Riesgo Aplicativo (ARA)** | Plantilla Seguridad TI FEMSA *(pendiente confirmar)* | Pre-QA | Cada cambio que afecte modelo de riesgo | `doc-writer` + Seguridad TI |
| 5 | **Modelado de Amenazas (MDA)** | `.tm7` (Microsoft Threat Modeling Tool) | Pre-QA | Cuando cambian flujos de datos críticos | `doc-writer` (narrativa) + LT (build .tm7) |
| 6 | **Principios de Seguridad (S-SDLC + Evidencias)** | Plantilla Seguridad TI FEMSA *(pendiente confirmar)* | Continuo | En cada release | `doc-writer` + LT |

> Los entregables 4-6 tienen plantillas corporativas internas que no están en mi posesión todavía. El `doc-writer` produce un draft con estructura razonable; el LT lo refina contra la plantilla oficial cuando esté disponible.

---

## Otros entregables del proyecto (no documentales)

Según el Training Material, además de los 6 documentos arriba, el proyecto entrega:

- **Paquetes de Instalación QA y PRD** — generados por CMS desde el pipeline.
- **Listas de verificación código y diseño** — checklists internos.
- **Checklist de NFR Soporte** — Non-Functional Requirements para operación.
- **Code Review (Peer Review)** — reportes del `code-reviewer` + revisión humana.
- **Resultado de Escaneo de Seguridad** — output de Checkmarx (opcional/recomendado).

---

## Versionado de los entregables

Convención incremental (la usa el agente `doc-writer`):

```
v0.1    Primera generación, draft inicial
v0.2 → v0.9    Iteraciones durante desarrollo (cada update suma)
v1.0    Primera versión liberada a producción
v1.1    Cambio menor post-producción (CHO de bugfix)
v2.0    Cambio mayor post-producción (CHO de funcionalidad mayor)
```

Cada bump agrega un renglón a la tabla **"Control de Versiones"** del documento, con:

| No. de Versión | Fecha de cambio | Autor | Comentarios |
|---|---|---|---|
| <X.Y> | DD/MM/YYYY | <nombre> | <referencia CHO / descripción del cambio> |

**Patrón observado** en `EspTécnica_PlanogramasCargaDiaria.docx`:
- 9 versiones acumuladas a lo largo del proyecto (jun 2021 → nov 2023).
- Cada versión asociada a un CHO específico (ej. CHG0037110, CHG0039135, CHG0039834).
- El documento NUNCA se regenera desde cero — solo se actualizan secciones afectadas.

---

## Estructura SharePoint para cada portal

Según el Training Material, cada portal tiene una carpeta en SharePoint corporativo con esta estructura:

```
<Portal Name>/
├── Documentos/
│   ├── Especificación Técnica/
│   │   └── Esp_Tecnica_<Portal>_v<X.Y>.docx
│   ├── Runbook/
│   │   └── Runbook_<Portal>_v<X.Y>.docx
│   ├── Matriz de Pruebas Unitarias/
│   │   └── Matriz_Pruebas_<Portal>_v<X.Y>.xlsx (o .docx)
│   ├── Análisis de Riesgos (ARA)/
│   │   └── ARA_<Portal>_v<X.Y>.docx
│   ├── Modelado de Amenazas (MDA)/
│   │   ├── MDA_<Portal>_v<X.Y>.tm7
│   │   └── MDA_<Portal>_v<X.Y>.docx  (narrativa adjunta)
│   ├── Principios de seguridad/
│   │   └── PrincipiosSeguridad_<Portal>_v<X.Y>.docx
│   ├── Readme/
│   └── Peer Review/
│
├── Change Request/  (por cada CHO)
│   └── <ID Proyecto>-CHG-<DescripcionCorta>/
│       ├── Especificación Técnica INTERFAZ.docx
│       ├── Runbook INTERFAZ.docx
│       ├── Evidencias de Pruebas Unitarias/
│       ├── Matriz de Pruebas Unitarias INTERFAZ.xlsx
│       ├── Matriz de Pruebas Mínimas INTERFAZ.xlsx
│       ├── Code Review INTERFAZ.docx
│       ├── Peer Review INTERFAZ.docx
│       └── Resultado de Escaneo de Seguridad/ (opcional)
│
├── Anexos/
│   └── (diagramas .drawio, .vsdx, etc.)
│
└── Deploy/
    └── (paquetes de instalación versionados)
```

> **Nota importante del Training Material:** "Especificación Técnica, Runbook y documento de mapeo son **incrementales**" — la versión que vive en SharePoint es siempre la última, las anteriores se preservan en historial de SharePoint.

> **Responsabilidad:** "Es responsabilidad del proveedor de servicios mantener y actualizar la documentación del componente que se está desarrollando o modificando."

---

## Cuándo se genera cada entregable

```
Fase del proyecto       Entregables que se actualizan
──────────────────────  ────────────────────────────────────────────
Diseño                  Esp Técnica v0.1
Análisis de riesgo      ARA v0.1, MDA v0.1
Construcción HU-001     Esp Técnica v0.2 (endpoint + entidad nueva)
                        Matriz Pruebas v0.1 (tests de HU-001)
Construcción HU-002     Esp Técnica v0.3 (más endpoints)
                        Matriz Pruebas v0.2 (tests HU-002)
Pre-QA                  Runbook v0.1 (procedimientos operativos)
                        ARA v0.2 (revisión completa)
                        MDA v0.2 (revisión completa)
                        Principios Seguridad v0.1 (S-SDLC inicial)
QA / UAT                Iteraciones según hallazgos
Liberación a PRD        Bump a v1.0 de todos los entregables
                        Principios Seguridad firmado
CHO post-producción     Bump del entregable afectado
```

---

## Contactos clave para los entregables

| Tema | Contacto |
|---|---|
| Especificación Técnica / Runbook | Líder Técnico del proyecto + EDA |
| Matriz de Pruebas | LT + Equipo QA |
| ARA + MDA | LT + Seguridad TI FEMSA |
| Principios Seguridad | LT + Seguridad TI |
| Plantillas oficiales (.dotx, .tm7) | EDA / Líder de Diseño / SharePoint corporativo |
| Estructura SharePoint del portal | Equipo CMS |

---

## Equipos de Soporte (post-producción)

| Área | Contacto |
|---|---|
| Equipo Soporte L2 WEB | aplicacionesweb@oxxo.com |
| Equipo Soporte L2 WF (Workflows) | usufcti.soporteworkflows@oxxo.com |
| Equipo Soporte L3 QA | usufcportall3qa@oxxo.com |
| Equipo Soporte L3 DEV / PRD | usufcappl3prd@oxxo.com |
| Data Center RyT Monterrey | dcmty.ryt@csc.femsa.com.mx |
| Seguridad Redes | oxxo.soc@axtel.com.mx |
| Soporte F5 (load balancer) | soporteoxxof5@connectua.com |

Todo apoyo solicitado va vía **Ticket en Service Now**. Los correos arriba son solo para dudas sobre la generación del ticket.

---

## Servidores de referencia (DEV)

Según WEB Training Material v3 (puede haber cambiado — verificar con CMS):

**App Servers (WebLogic):**
```
Fcowldes.femcom.net      10.80.3.202
Fcowldes02.femcom.net    10.80.3.192
Fcowldes05.femcom.net    172.26.142.132
Fcowldes06.femcom.net    172.26.142.133
Fcowldes07.femcom.net    172.26.142.134
Fcowldes08.femcom.net    192.168.54.67
Fcowldes09.femcom.net    172.26.142.136
Oivcntd1.femcom.net      10.186.13.23
```

**BD Servers (Oracle):**
```
EXPANDES  →  oxvwfed1.femcom.net:1521         expandes.femcom.net
FCIASDES  →  oxfbdfciasd00.femcom.net:1535    FCIASDES.FEMCOM.NET
FCOACDES  →  fcocbdd1.femcom.net:1521          FCOACDES.FEMCOM.NET
FCPVEDES  →  (consultar Training Material)
```

---

## Referencias cruzadas
- Agente `doc-writer.md` — genera los 6 entregables incrementalmente
- `scripts/generate-docs.py` — convierte `.md` a `.docx` con header FEMSA Comercio
- `seguridad-checklist.md` — informa contenido del ARA y MDA
- `.claude/project-config.yml` — campo `documentation.current_versions` rastrea versión vigente de cada entregable
