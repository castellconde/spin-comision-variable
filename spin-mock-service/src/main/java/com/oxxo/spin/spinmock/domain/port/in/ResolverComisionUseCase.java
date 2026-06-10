package com.oxxo.spin.spinmock.domain.port.in;

import com.oxxo.spin.spinmock.domain.model.RespuestaComision;
import com.oxxo.spin.spinmock.domain.model.SolicitudComision;

/** Caso de uso: resolver la respuesta simulada para una solicitud, segun los escenarios en BD. */
public interface ResolverComisionUseCase {
    RespuestaComision resolver(SolicitudComision solicitud);
}
