package com.oxxo.spin.comisionvariable.api.exception;

/**
 * Falla al consumir el API externo de Spin (tras agotar reintentos,
 * timeout, o circuito abierto). Se mapea a HTTP 502 / 503.
 */
public class SpinIntegrationException extends RuntimeException {

    private final int status;

    public SpinIntegrationException(String mensaje, int status, Throwable cause) {
        super(mensaje, cause);
        this.status = status;
    }

    public SpinIntegrationException(String mensaje, int status) {
        this(mensaje, status, null);
    }

    public int getStatus() {
        return status;
    }
}
