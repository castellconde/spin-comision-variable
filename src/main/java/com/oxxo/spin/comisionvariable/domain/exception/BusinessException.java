package com.oxxo.spin.comisionvariable.domain.exception;

/**
 * Violacion de una regla de negocio del dominio. Excepcion pura del nucleo.
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
