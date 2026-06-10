package com.oxxo.spin.spinmock.domain.model;

/**
 * Resultado resuelto por el dominio: qué status devolver, con qué retardo y
 * qué cuerpo ({@link ComisionBody} si es exito, {@link ErrorBody} si no).
 */
public record RespuestaComision(int httpStatus, long delayMs, Object body) {
}
