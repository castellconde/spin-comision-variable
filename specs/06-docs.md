### 6.1 Los 6 entregables del proyecto

| Entregable | Formato final | Cómo generar |
|---|---|---|
| Especificación Técnica | `.docx` | `doc-writer` → `generate-docs.py` |
| Runbook | `.docx` | `doc-writer` → `generate-docs.py` |
| Matriz Pruebas Unitarias | `.xlsx` | `doc-writer` → `generate-test-matrix.py` |
| Análisis Riesgo Aplicativo (ARA) | `.docx` | `doc-writer` → `generate-docs.py` |
| Modelado de Amenazas (MDA) | `.docx` + `.tm7` | `doc-writer` → `generate-docs.py` (el `.tm7` lo arma el LT en Microsoft Threat Modeling Tool con el checklist del .md) |
| Principios de Seguridad | `.docx` | `doc-writer` → `generate-docs.py` |
| Matriz Pruebas QA E2E | `.xlsx` | `qa-automation` → Playwright → `qa/scripts/generate-qa-matrix.js` |

### 6.2 Flujo paso a paso (ejemplo: Especificación Técnica)

**Paso 1 · Pide al agente que genere el `.md`:**
```powershell
claude
> Usa el agente doc-writer para generar la Especificación Técnica basándote en
  docs/specs/ y el código actual. Genera v0.1.
```

El agente produce `docs/Esp_Tecnica_v0.1.md` con la estructura oficial FCTI (Identificación, Control de Versiones, Presentación, Arquitectura, Requerimientos, Diseño Técnico, etc.).

**Paso 2 · Convierte a `.docx` con header FEMSA Comercio:**
```powershell
python scripts/generate-docs.py docs/Esp_Tecnica_v0.1.md
```

Salida: `docs/Esp_Tecnica_v0.1.docx` listo para subir a SharePoint.

**El script detecta el tipo de documento automáticamente** por el nombre del archivo:
- `Esp_Tecnica_*` → header "Especificación Técnica · FCTI_CNF_Especificación Técnica · REVISIÓN 5"
- `Runbook_*` → header "Runbook · FCTI_CNF_Runbook · REVISIÓN 2"
- `Matriz_Pruebas_*` → matriz Excel
- `ARA_*` → Análisis de Riesgo Aplicativo
- `MDA_*` → Modelado de Amenazas
- `Principios_Seguridad_*` → S-SDLC

### 6.3 Actualización incremental (sprint a sprint)

Cuando llega un nuevo sprint con cambios:

```powershell
claude
> Usa el agente doc-writer para actualizar la Especificación Técnica con los
  cambios de HU-003. La versión vigente es v0.1 — lee, identifica deltas,
  modifica solo secciones afectadas, agrega renglón a Control de Versiones,
  guarda como v0.2.
```

Después regeneras el `.docx`:
```powershell
python scripts/generate-docs.py docs/Esp_Tecnica_v0.2.md
```

### 6.4 Matriz de Pruebas Unitarias en Excel

```powershell
# El agente doc-writer genera el .md con las pruebas
claude
> Usa doc-writer para generar la Matriz de Pruebas Unitarias basándote en los tests existentes

# Convierte a Excel
python scripts/generate-test-matrix.py docs/Matriz_Pruebas_Unitarias_v0.1.md
```

Excel resultante: header rojo OXXO, casos coloreados PASA/FALLA, resumen con totales.

---