package com.oxxo.spin.comisionvariable.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Modelo de dominio del resultado: la comision variable calculada.
 * Clase pura, sin frameworks.
 */
public record Comision(
        String id,
        String plaza,
        String tienda,
        String caja,
        String concepto,
        BigDecimal porcentajeComision,
        BigDecimal montoComision,
        String moneda,
        OffsetDateTime calculadaEn) {
}
