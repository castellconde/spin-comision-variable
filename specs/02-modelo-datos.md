# 02 — Modelo de Datos

> El doc-writer usa este archivo + las entidades JPA reales para generar la sección 4.1.3 (Diagrama Entidad-Relación) de la Especificación Técnica.

No se tienen tablas definidas

Sugiere lo mejor de acuerdo al framework quarkus y la información que maneja, crea una tabla bitacora para registrar cada consulta realizada al servicio externo, payload de request y payload de respuesta.

Crear tabla de configuración para parametros de consumo de servicios externos, ejemplo client_id, secret_id,  parametros de configuración para manteniemiento de tabla transaccional, ejemplo bitacora, mantener particiones futuras a 12 meses, o depurar particiones antiguas a 6 meses

Implementa redis de forma opcional, en caso de no implementarse no se rompa el servicio