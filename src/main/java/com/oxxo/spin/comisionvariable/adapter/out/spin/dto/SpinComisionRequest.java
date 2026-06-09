package com.oxxo.spin.comisionvariable.adapter.out.spin.dto;

/** Payload enviado al API de Spin. */
public record SpinComisionRequest(
        String referencia,
        String plaza,
        String tienda,
        String caja,
        String transaccion,
        String producto,
        String servicio) {
}
