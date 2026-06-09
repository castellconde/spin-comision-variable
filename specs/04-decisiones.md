# 04 — Decisiones técnicas

> Bitácora de decisiones tomadas durante el proyecto. Empieza casi vacío y crece durante el sprint. El doc-writer lo lee para llenar la sección "Diseño Técnico" de la Esp Técnica.

## Convención

Cada decisión sigue este formato. Las decisiones se acumulan (nunca se borran), solo se marcan como "Superseded" si una decisión nueva las reemplaza.

---

## JUN-001 Piloto Comision Variable

**Fecha:** [08/06/2026]
**Contexto:** Crear piloto para definir arquitectura y tomar decisiones relacionadas a la habilitación de infraestructura
**Decisión:** Construir piloto con ambiente local
**Razón:** No existe infraestructura y se requiere evaluar componentes a reutilizar en nube
**Implicación:** Define siguientes pasos para desarrollo acelerado
**Alternativas descartadas:** No existen alternativas aun

---

## JUN-002 Arquitectura hexagonal obligatoria

**Fecha:** [08/06/2026]
**Contexto:** El piloto inicial quedó como arquitectura por capas con dependencias del dominio hacia los adaptadores concretos y fuga de DTOs/entidades al núcleo.
**Decisión:** **REQUISITO permanente** — todos los microservicios del proyecto deben implementar **arquitectura hexagonal (puertos y adaptadores)**:
- `domain/` puro, sin frameworks (sin Quarkus, Jakarta, Jackson, JPA, SmallRye).
- Puertos de entrada (casos de uso) y de salida definidos por el dominio; los adaptadores los implementan.
- `application/` orquesta dependiendo solo de puertos y modelos de dominio.
- `adapter/in` (driving) y `adapter/out` (driven) con mappers DTO ↔ dominio ↔ contrato externo.
- La dirección de dependencias siempre apunta hacia el dominio.
**Razón:** Aislar el núcleo de la tecnología, mejorar testeabilidad (pruebas unitarias puras) y facilitar el reemplazo de proveedores (Spin, BD, cache) sin tocar la lógica de negocio.
**Implicación:** Aplica a este piloto (ya refactorizado) y a futuros servicios. Cualquier PR que acople el dominio a infraestructura debe rechazarse.
**Alternativas descartadas:** Arquitectura por capas tradicional (descartada por acoplamiento del dominio a la infraestructura).
**Asegurarse de que todas las clases candidatas a pruebas unitarias tengan el test correspondiente para la cobertura de codigo mayo a 90%

