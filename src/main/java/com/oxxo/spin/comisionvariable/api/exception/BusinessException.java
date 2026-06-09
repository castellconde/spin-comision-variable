package com.oxxo.spin.comisionvariable.api.exception;

/**
 * Error de regla de negocio (HTTP 422 / 400 segun el caso).
 */
public class BusinessException extends RuntimeException {

    private final String codigo;
    private final int status;

    public BusinessException(String codigo, String mensaje, int status) {
        super(mensaje);
        this.codigo = codigo;
        this.status = status;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getStatus() {
        return status;
    }
}
