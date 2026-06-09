package com.oxxo.spin.comisionvariable.domain.model;

/**
 * Registro de dominio de un consumo al proveedor externo, destinado a la
 * bitacora. El adaptador de persistencia decide como serializar/almacenar
 * (JSON, columnas, traza); el nucleo solo expresa QUE se registra.
 */
public record RegistroConsumo(
        Consulta consulta,
        Comision comision,           // null si hubo fallo
        ResultadoConsulta resultado,
        String errorCodigo,          // null si OK
        int httpStatus,
        String errorMensaje,         // null si OK
        long latenciaMs) {

    public static RegistroConsumo exito(Consulta consulta, Comision comision, long latenciaMs) {
        return new RegistroConsumo(consulta, comision, ResultadoConsulta.OK, null, 200, null, latenciaMs);
    }

    public static RegistroConsumo fallo(Consulta consulta, ResultadoConsulta resultado,
                                        String errorCodigo, int httpStatus, String errorMensaje, long latenciaMs) {
        return new RegistroConsumo(consulta, null, resultado, errorCodigo, httpStatus, errorMensaje, latenciaMs);
    }
}
