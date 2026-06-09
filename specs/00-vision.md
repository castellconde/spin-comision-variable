# 00 — Visión del proyecto

> Este archivo es **input humano**. El LT lo llena antes de invocar a Claude. Los agentes leen este archivo para entender qué construir.

## Nombre del proyecto
Piloto Spin - Comision Variable

## Nombre corto
Spin - Comision Variable

## Dueño funcional / Patrocinador
[REEMPLAZAR — nombre · puesto · correo]

## Líder Técnico (LT) responsable
Roberto Castillo

## Frente del EDT al que pertenece
Ingeniería de Soluciones

---

## Problema que resuelve
Implementar una arquitectura de referencia para proyectos en la nube basada en microservicios, Se requiere hacer una implementación limpia, segura y confiable de microservicios para consulta de comisiones desde servicios OnPremise expuestos en Cloud

## Alcance funcional
Consultar Comisiones Variables desde Punto de Venta

- Gestión de comisiones por transacción, consultar comisiones con datos de entrada por plaza, tienda, caja, transaccion, producto y/o servicio
- Servicio en la Nube AWS ROSA que exponga un endpoint y a su vez consuma un API del proveedor Spin, manteniendo la trazabilidad de la consulta.
- Seguridad implementando Keycloak para autenticar servicio consumidor, crear REALM para apps
- Bitacora con tabla en postgresql con detalle de consumo a api externa Spin
- Integración con Datadog por agente, agregar logs para trazabilidad de transacciones
- Integración con Conjur por agente

## No contempla
- No contempla servicios FrontEnd

## Usuarios objetivo
- Usuarios de punto de venta, consultar comision variable de productos y/o servicios

## Métricas de éxito
- Un servicio estable y confiable, se ha probado estabilidad y rendimiento de microservicios en Quarkus, aprovechar todas sus ventajas contra otros frameworks como springboot
