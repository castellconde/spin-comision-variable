package com.oxxo.spin.comisionvariable.domain.exception;

import com.oxxo.spin.comisionvariable.domain.model.ResultadoConsulta;

/**
 * Falla del proveedor de comisiones (puerto de salida). Es una excepcion de
 * dominio: NO menciona Spin, HTTP, ni la libreria de fault tolerance. El
 * adaptador traduce sus fallas tecnicas a esta excepcion, de modo que el
 * nucleo permanezca agnostico de la tecnologia de integracion.
 */
public class ComisionProviderException extends RuntimeException {

    private final String codigo;
    private final int status;
    private final ResultadoConsulta resultado;

    public ComisionProviderException(String codigo, String mensaje, int status,
                                     ResultadoConsulta resultado, Throwable cause) {
        super(mensaje, cause);
        this.codigo = codigo;
        this.status = status;
        this.resultado = resultado;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getStatus() {
        return status;
    }

    public ResultadoConsulta getResultado() {
        return resultado;
    }
}
