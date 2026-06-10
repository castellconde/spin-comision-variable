package com.oxxo.spin.spinmock.adapter.out.persistence;

import com.oxxo.spin.spinmock.domain.model.Escenario;

/** Traduce entre la entidad JPA y el modelo de dominio. */
final class EscenarioMapper {

    private EscenarioMapper() {
    }

    static Escenario toDomain(EscenarioEntity e) {
        return new Escenario(
                e.id, e.nombre, e.activo, e.prioridad, e.matchCampo, e.matchRegex,
                e.httpStatus, e.porcentajeComision, e.montoComision, e.moneda,
                e.estatus, e.errorCodigo, e.errorMensaje, e.delayMs);
    }

    /** Copia los campos de negocio del dominio a la entidad (no toca id ni fechas). */
    static void copyToEntity(Escenario d, EscenarioEntity e) {
        e.nombre = d.nombre();
        e.activo = d.activo();
        e.prioridad = d.prioridad();
        e.matchCampo = d.matchCampo();
        e.matchRegex = d.matchRegex();
        e.httpStatus = d.httpStatus();
        e.porcentajeComision = d.porcentajeComision();
        e.montoComision = d.montoComision();
        e.moneda = d.moneda();
        e.estatus = d.estatus();
        e.errorCodigo = d.errorCodigo();
        e.errorMensaje = d.errorMensaje();
        e.delayMs = (int) d.delayMs();
    }
}
