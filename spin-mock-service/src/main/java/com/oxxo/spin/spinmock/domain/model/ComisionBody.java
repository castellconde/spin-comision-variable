package com.oxxo.spin.spinmock.domain.model;

import java.math.BigDecimal;

/** Cuerpo de respuesta exitosa (contrato Spin). */
public record ComisionBody(
        String referencia,
        String concepto,
        BigDecimal porcentajeComision,
        BigDecimal montoComision,
        String moneda,
        String estatus) {
}
