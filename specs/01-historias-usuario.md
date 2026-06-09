# 01 — Historias de Usuario

> Las HUs son el contrato del sprint. Sin criterios de aceptación claros, Claude inventa y tiras código. Toma el tiempo necesario aquí.

## Convención

Cada HU sigue este formato. Copia el bloque "HU-EJEMPLO" abajo, duplícalo para cada HU que necesites.

---

## HU-001  Servicio de consulta de Comisiones Variables

**Como** Punto de venta consulta comision variable de productos y/o servicios
**Quiero** Construir servicio con baja latencia y evitar interrupción del servicio
**Para** Mejorar Velocidad de consulta

### Criterios de aceptación

[REEMPLAZAR con lista concreta. Ejemplos del estilo a usar:]
- POST `/rest/v1/comision-variable/comision` con body válido retorna 200 + Location header.

### Validaciones

[REEMPLAZAR — qué se valida server-side]
- `id`: obligatorio, 3-100 chars, regex `^[A-Za-z0-9 ñÑ_-]+$`.
- `plaza`: obligatorio, 3-100 chars, regex `^[A-Za-z0-9 ñÑ_-]+$`.
- `tienda`: obligatorio, 3-100 chars, regex `^[A-Za-z0-9 ñÑ_-]+$`.
- `caja`: obligatorio, 3-100 chars, regex `^[A-Za-z0-9 ñÑ_-]+$`.

### Notas técnicas (opcional)

Servicio de solo consulta, exponer endpoints health check para monitorear contenedores

---
