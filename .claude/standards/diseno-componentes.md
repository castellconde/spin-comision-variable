# Componentes Visuales — Sistema de Diseño Portales Web OXXO/FEMSA

> Resumen citable del **Sistema de Diseño de Portales Web** (EstandaresDiseño-PortalesWebOXXO.pdf, Ene 2024) — UI/UX XPOS Team.
>
> Cita: *"Sistema de Diseño Portales Web §5.1.1"*.
>
> Los tokens hex específicos (colores) y sizes están en `.claude/standards/diseno-tokens.yml`. Este archivo describe **cómo se ven y comportan los componentes**, no los valores. La paleta activa la define `.claude/project-config.yml → design.palette`.

---

## §1. Estructura general de un portal web

Cada portal sigue esta estructura vertical (de arriba a abajo):

```
┌──────────────────────────────────────┐
│ Header             84px              │  ← Logo del portal + identidad
├──────────────────────────────────────┤
│ Nav Bar            60px              │  ← Menú principal
├──────────────────────────────────────┤
│                                      │
│ Content            (flexible)        │  ← Páginas del portal
│                                      │
├──────────────────────────────────────┤
│ Advice Note        40px              │  ← Avisos del sistema (Mesa de Ayuda, etc.)
├──────────────────────────────────────┤
│ Footer             56px              │  ← Versión AC + fecha actualización
└──────────────────────────────────────┘
   Ancho base: 1366px
```

### Grid responsivo
- **12 columnas** + **11 medianiles (gutters)** + **2 márgenes laterales**.
- Columna: 84px · Medianil: 24px · Margen lateral: 48px.

---

## §2. Botones

### §2.1 Botones primarios (CTAs principales)

Para la paleta activa, el botón primario usa el color CTA principal:
- Paleta OXXO: Fondo `red_oxxo` (`#DF0024`) o `yellow_oxxo` (`#F6D300`).
- Paleta FEMSA: Fondo `cherry_femsa` (`#98002E`).

**Especificación:**
- Texto: Open Sans Bold 16pt, color `white_message`.
- Padding interno: 6px horizontal, 8px vertical (aprox).
- Altura: 40-52px según contexto.
- Esquinas: ligero radius (6px).
- Estados: Normal · Hover (oscurece 10%) · Desplegado · Disabled.

**Ejemplos del PDF:**
- `Guardar` — Normal + Hover
- `Cambiar Proveedor` — Normal + Hover
- `Mostrar más` / `Mostrar menos` — con flecha indicadora ↕

### §2.2 Botones secundarios

- Fondo: blanco (`white_portal`).
- Borde: 1px en color CTA primario.
- Texto: Open Sans Bold 16pt en color CTA primario.
- Estados: Normal · Hover (fondo se llena del color CTA) · Disabled.

Ejemplos: `Agregar`, `Cambiar Proveedor` (versión secundaria).

### §2.3 Botones terciarios

- Sin fondo ni borde — solo ícono + texto.
- Texto: Open Sans Bold 14-16pt en color CTA primario.
- Ícono a la izquierda.
- Estados: Normal · Hover (subraya o intensifica color).

Ejemplos: `🗑 Eliminar Datos`, `📅 Etiqueta`.

---

## §3. Inputs y campos de entrada

### §3.1 Inputs de texto
- Etiqueta arriba: Open Sans Bold 14pt, color `black_message`.
- Campo: borde 1px `gray_silver`, padding 12px, altura 40px.
- Texto capturado: Open Sans Regular 16pt, color `black_message`.
- Placeholder: color `gray_question`.
- Estados:
  - **Normal:** borde `gray_silver` 1px.
  - **Display:** muestra valor capturado.
  - **Hover/Focus:** borde 2px en color CTA.
  - **Bloqueado:** fondo `gray_light`, sin interacción.
  - **Error:** borde rojo `error` + mensaje debajo.

### §3.2 Select / Dropdown
- Diseño igual al input + ícono de chevron a la derecha.
- Estados: Normal · Hover · Desplegado (muestra opciones con `white_portal` fondo y `gray_light` para selección hover).

### §3.3 Buscador
- Input + ícono de lupa a la izquierda o derecha.
- Botón de buscar suele ser primario (color CTA).

### §3.4 Date picker (calendario)
- Mismo input + ícono de calendario.
- Desplegado: calendario blanco con texto `black_portal`, día seleccionado en color CTA.

---

## §4. Listas y tablas

### §4.1 Lista simple
- Filas alternadas (zebra) opcional con `gray_light`.
- Fila seleccionada: borde izquierdo de 4px en color CTA primario.
- Texto: Open Sans Regular 14pt.

### §4.2 Tabla con columnas
- Header: fondo `gray_light`, texto Open Sans Bold 14pt `black_message`.
- Filas: fondo `white_portal`, texto Open Sans Regular 14pt `black_message`.
- Acciones por fila (íconos): editar, ver detalles, eliminar.

---

## §5. Navegación

### §5.1 Header
- Fondo: color CTA primario (negro en algunos casos, según portal).
- Logo del portal a la izquierda, menú principal al centro/derecha.
- Buscador global integrado (opcional).

### §5.2 Tabs (pestañas)
- Texto: Open Sans Bold 14pt.
- Tab activa: borde inferior de 4px en color CTA + texto en color CTA.
- Tabs inactivas: texto `black_message`.

### §5.3 Breadcrumbs
- Texto: Open Sans Regular 14pt `gray_question` para items anteriores.
- Item actual: Open Sans Bold 14pt color CTA.
- Separador: `/` con espacios.

### §5.4 Paginación
- Texto: Open Sans Bold 14pt.
- Página actual: fondo color CTA, texto `white_message`.
- Otras páginas: texto color CTA, sin fondo.
- Botones "Anterior" / "Siguiente".

---

## §6. Indicadores

### §6.1 Indicador de pasos (Stepper)
- Círculos numerados conectados por líneas.
- Paso completado: fondo `success` (`#79C257`), borde 2px.
- Paso actual: borde 1px `gray_proximidad`, fondo `gray_light`, número en color CTA.
- Paso futuro: borde 2px `gray_light`, número `black_message` regular.

### §6.2 Indicador de progreso
- Barra: fondo `gray_light`, fill en color CTA primario.
- Texto del porcentaje a la derecha (opcional).

### §6.3 Radio buttons
- Diseño 1: círculos.
- Diseño 2: rectángulos tipo "chip" con texto.
- Seleccionado: borde color CTA + fondo `gray_light`.

### §6.4 Checkboxes
- Cuadrado borde 1px `gray_silver`.
- Seleccionado: fondo color CTA + check `white_message`.

---

## §7. Modales / Ventanas de aviso

Función: mostrar contenido relevante que solo se usa en el momento, bloquea funciones del portal y concentra el foco.

### §7.1 Modal con botón (confirmación)
- Fondo: `white_portal`, borde 2px `gray_light`.
- Drop shadow: negro, blur 7, opacidad 25%.
- Padding interno: 48px arriba/abajo, 24px laterales.
- Texto: Open Sans Regular 20pt `black_message`.
- Botones al fondo: `Guardar` (primario) + `Cancelar` (secundario).
- Ancho: 5 columnas + 4 medianiles del grid.

### §7.2 Modal sin botón (informativo)
- Diseño similar al anterior, sin botones — solo un `Aceptar` o se cierra solo después de timeout.

### §7.3 Modal grande (6 u 8 columnas)
- Para formularios completos dentro del modal.
- Header con título + botón X de cierre.
- Body con campos editables.
- Footer con `Guardar` + `Cancelar`.

---

## §8. Mensajes semánticos

Colores semánticos del `diseno-tokens.yml`:

| Tipo | Color | Uso |
|---|---|---|
| Éxito | `success` (`#79C257`) | Operación completada |
| Error | `error` (`#ED1A35`) | Operación falló |
| Alerta | `warning` (`#F36E52`) | Advertencia importante |
| Confirmación | `confirmation` (`#62625C`) | Confirmación neutra |
| Favorito | `favorite` (`#FFC629`) | Marcadores / preferencias |
| Información | `info` (`#29A5FF`) | Mensaje informativo |

Iconos correspondientes vienen de Boxicons / Flaticon (ver `diseno-tokens.yml` § iconography).

---

## §9. Casos de uso documentados en el PDF

El sistema de diseño incluye ejemplos de pantallas reales que sirven de referencia:

1. **Login a OXXO** — header negro, logo centrado, inputs blancos sobre fondo oscuro, botón primario rojo.
2. **Dashboard inicial** — tarjetas con `Mostrar más`, navegación lateral de secciones.
3. **Catálogo de Artículos** — tabla con filtros arriba, paginación abajo, acciones por fila.
4. **Confirmación de cambios** — modal centrado con botones primario y secundario.

Para una pantalla nueva, usar estos casos como referencia visual.

---

## Validación automática

El agente `code-reviewer` valida en componentes Angular (`*.component.scss`, `*.component.ts`):

- ✅ Colores hex usados están en la paleta activa (`design.palette` de project-config).
- ✅ Tipografía es Open Sans (font-family declarada en `:root` o componente).
- ✅ Iconos vienen de Boxicons o Flaticon (no SVGs random).
- ✅ Espaciados respetan el grid (múltiplos del column_width_px / gutter_px).

Hallazgos típicos:
- 🟡 `color: #FF0000` cuando la paleta activa es OXXO (debería ser `#DF0024`).
- 🟡 `font-family: Roboto` (debería ser Open Sans).
- 🟡 Padding/margin con valores arbitrarios (`13px`, `17px`) que no son múltiplos del sistema.

---

## Referencias cruzadas
- `diseno-tokens.yml` — valores hex y sizes machine-readable
- `codigo-angular.md` — convenciones técnicas Angular
- EstandaresDiseño-PortalesWebOXXO.pdf — documento fuente completo con ejemplos visuales
