package com.oxxo.spin.spinmock.domain.model;

/** Payload que el servicio bajo prueba envia al "API Spin". */
public record SolicitudComision(
        String referencia,
        String plaza,
        String tienda,
        String caja,
        String transaccion,
        String producto,
        String servicio) {

    /** Valor del campo indicado, para evaluar el disparador del escenario. */
    public String valorDe(String campo) {
        if (campo == null) {
            return null;
        }
        return switch (campo) {
            case "transaccion" -> transaccion;
            case "servicio" -> servicio;
            case "producto" -> producto;
            case "plaza" -> plaza;
            case "tienda" -> tienda;
            case "caja" -> caja;
            default -> null;
        };
    }

    public String concepto() {
        if (servicio != null && !servicio.isBlank()) {
            return servicio;
        }
        return producto;
    }
}
