package com.oxxo.spin.comisionvariable.domain.port.out;

import com.oxxo.spin.comisionvariable.domain.model.RegistroConsumo;

/**
 * Puerto de SALIDA (driven): persistencia de la bitacora de consumo.
 * El adaptador (JPA/PostgreSQL) decide el almacenamiento; el nucleo solo
 * expresa la intencion de registrar.
 */
public interface BitacoraPort {

    void registrar(RegistroConsumo registro);
}
