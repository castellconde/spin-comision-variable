package com.oxxo.spin.spinmock.domain.model;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Escenario administrable del mock. Si {@code matchRegex} es null, es el
 * escenario por defecto (comodin). El menor {@code prioridad} se evalua primero.
 */
public record Escenario(
        Long id,
        String nombre,
        boolean activo,
        int prioridad,
        String matchCampo,
        String matchRegex,
        int httpStatus,
        BigDecimal porcentajeComision,
        BigDecimal montoComision,
        String moneda,
        String estatus,
        String errorCodigo,
        String errorMensaje,
        long delayMs) {

    /** ¿Aplica este escenario a la solicitud dada? */
    public boolean aplicaA(SolicitudComision s) {
        if (matchRegex == null || matchRegex.isBlank()) {
            return true; // comodin / default
        }
        String valor = s.valorDe(matchCampo);
        if (valor == null) {
            return false;
        }
        try {
            return Pattern.matches(matchRegex, valor);
        } catch (RuntimeException e) {
            return false; // regex invalida: no aplica
        }
    }

    public boolean esExito() {
        return httpStatus >= 200 && httpStatus < 300;
    }
}
