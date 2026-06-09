package com.oxxo.spin.comisionvariable.domain.port.in;

import com.oxxo.spin.comisionvariable.domain.model.Comision;
import com.oxxo.spin.comisionvariable.domain.model.Consulta;

/**
 * Puerto de ENTRADA (driving): caso de uso de consulta de comision variable.
 * Los adaptadores de entrada (REST, mensajeria, etc.) dependen de esta
 * interfaz, nunca de la implementacion concreta.
 */
public interface ConsultarComisionUseCase {

    Comision consultar(Consulta consulta);
}
