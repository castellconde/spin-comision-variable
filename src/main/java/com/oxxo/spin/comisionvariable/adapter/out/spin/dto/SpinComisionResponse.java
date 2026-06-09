package com.oxxo.spin.comisionvariable.adapter.out.spin.dto;

import java.math.BigDecimal;

/** Respuesta del API de Spin con la comision calculada. */
public record SpinComisionResponse(
        String referencia,
        String concepto,
        BigDecimal porcentajeComision,
        BigDecimal montoComision,
        String moneda,
        String estatus) {
}
