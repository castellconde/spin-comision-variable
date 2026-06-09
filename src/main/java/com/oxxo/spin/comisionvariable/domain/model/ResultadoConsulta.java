package com.oxxo.spin.comisionvariable.domain.model;

/** Resultado del intento de consulta hacia el proveedor, para la bitacora. */
public enum ResultadoConsulta {
    OK,
    ERROR,
    TIMEOUT,
    CIRCUIT_OPEN,
    RATE_LIMITED
}
