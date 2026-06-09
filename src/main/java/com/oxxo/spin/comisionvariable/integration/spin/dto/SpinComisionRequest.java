package com.oxxo.spin.comisionvariable.integration.spin.dto;

/** Payload enviado al API de Spin para calcular la comision. */
public record SpinComisionRequest(
        String referencia,
        String plaza,
        String tienda,
        String caja,
        String transaccion,
        String producto,
        String servicio) {
}
