package com.oxxo.spin.comisionvariable.domain.port.out;

import com.oxxo.spin.comisionvariable.domain.model.Comision;
import com.oxxo.spin.comisionvariable.domain.model.Consulta;

/**
 * Puerto de SALIDA (driven): obtencion de la comision desde un proveedor
 * externo. La implementacion concreta (adaptador Spin) decide protocolo,
 * autenticacion y politicas de resiliencia. Ante un fallo lanza
 * {@link com.oxxo.spin.comisionvariable.domain.exception.ComisionProviderException}.
 */
public interface ComisionProviderPort {

    Comision obtenerComision(Consulta consulta);
}
