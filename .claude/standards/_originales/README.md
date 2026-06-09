# Documentos Originales — Estándares Corporativos OXXO/FEMSA

> Esta carpeta es para los documentos corporativos completos (DOCX, PDF, PPTX) que sirven de fuente única de verdad. **No se versionan en Git por confidencialidad.**

## Cómo poblar esta carpeta

El LT descarga estos archivos desde SharePoint corporativo al clonar el template a un proyecto nuevo:

| Archivo | Vive en SharePoint | Vigencia |
|---|---|---|
| `Documento_de_estándares_5_0.docx` | `http://ti.comercio.fne/centrocomptec/wf/WF_Estandares/` | 2021 (parcialmente vigente) |
| `STTI_Estandar_de_Codificacion_Segura_-_JAVA_v2.docx` | `http://ti/NE/pweb/Estandares/STTI/` | Ene 2023 (vigente) |
| `STTI_Estándar_de_Codificacion_Segura_-_PL_SQL_v2.docx` | `http://ti/NE/pweb/Estandares/STTI/` | Ene 2023 (vigente) |
| `STTI_Estándar_de_Codificación_Segura__Angular_v2.docx` | `http://ti/NE/pweb/Estandares/STTI/` | Ene 2023 (vigente) |
| `EstandaresDiseño-PortalesWebOXXO.pdf` | SharePoint UI/UX XPOS Team | Ene 2024 |
| `WEB_-_Training_Material.pptx` | SharePoint Onboarding | V3 (vigente) |
| `FCTI_CNF_Especificación_Técnica_Rev_5.dotx` | SharePoint plantillas FCTI | Vigente |
| `FCTI_CNF_PGD_Runbook_Rev_2.dotx` | SharePoint plantillas FCTI | Vigente |

## Para qué los necesitas tener localmente

1. **Consulta de detalle:** cuando un agente cita una regla (ej. "STTI Java v2 §3.2") y necesitas ver el contexto completo en el documento original.
2. **Generación de .docx oficiales:** el script `scripts/generate-docs.py` puede usar los `.dotx` como plantilla base para preservar fielmente los estilos corporativos (logos, headers, footers, fuentes). Sin los `.dotx`, el script usa estilos reconstruidos que son visualmente equivalentes pero no byte-idénticos.
3. **Onboarding de nuevos LTs:** estos son los documentos que el LT debe leer (al menos las secciones que aplican a su rol) antes de empezar.

## .gitignore

Esta carpeta está incluida en `.gitignore` del template:
```
.claude/standards/_originales/*
```

Esto evita que alguien accidentalmente commitee documentos corporativos confidenciales al repo público de GitHub.

## Cuando hay versiones nuevas en SharePoint

1. **Descarga el archivo actualizado** y reemplázalo en `_originales/`.
2. **Actualiza el `.md` consolidado correspondiente** en `.claude/standards/` con las reglas nuevas/cambiadas, preservando la numeración de secciones.
3. **Actualiza la tabla "Última sincronización"** en `.claude/standards/00-INDEX.md`.
4. **Propone PR al template upstream** si el cambio es relevante para todos los proyectos.

---

**Si esta carpeta está vacía, los agentes aún funcionan** — leen los `.md` consolidados que sí están versionados. Los originales son solo para consulta de detalle y para mayor fidelidad del `.docx` final.
