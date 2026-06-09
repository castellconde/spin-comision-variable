package com.oxxo.spin.comisionvariable.domain.model;

/**
 * Modelo de dominio de una consulta de comision variable.
 *
 * <p>NUCLEO HEXAGONAL: clase pura, sin dependencias de frameworks (ni Jakarta,
 * ni Jackson, ni Quarkus). La validacion sintactica vive en el adaptador de
 * entrada; aqui solo reside la regla de negocio invariante.</p>
 */
public record Consulta(
        String id,
        String plaza,
        String tienda,
        String caja,
        String transaccion,
        String producto,
        String servicio) {

    /** Regla de negocio: debe venir al menos producto o servicio. */
    public boolean tieneProductoOServicio() {
        return (producto != null && !producto.isBlank())
                || (servicio != null && !servicio.isBlank());
    }

    /** Concepto evaluado (servicio tiene prioridad sobre producto). */
    public String concepto() {
        if (servicio != null && !servicio.isBlank()) {
            return servicio;
        }
        return producto;
    }
}
