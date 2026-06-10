package com.oxxo.spin.spinmock.application;

import com.oxxo.spin.spinmock.domain.model.ComisionBody;
import com.oxxo.spin.spinmock.domain.model.Escenario;
import com.oxxo.spin.spinmock.domain.model.ErrorBody;
import com.oxxo.spin.spinmock.domain.model.RespuestaComision;
import com.oxxo.spin.spinmock.domain.model.SolicitudComision;
import com.oxxo.spin.spinmock.domain.port.in.ResolverComisionUseCase;
import com.oxxo.spin.spinmock.domain.port.out.EscenarioRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;

/**
 * Resuelve la respuesta simulada: toma el primer escenario activo (por
 * prioridad) que aplica a la solicitud y arma el cuerpo correspondiente.
 */
@ApplicationScoped
public class ResolverComisionService implements ResolverComisionUseCase {

    private final EscenarioRepositoryPort repositorio;

    @Inject
    public ResolverComisionService(EscenarioRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public RespuestaComision resolver(SolicitudComision solicitud) {
        Escenario esc = repositorio.activosPorPrioridad().stream()
                .filter(e -> e.aplicaA(solicitud))
                .findFirst()
                .orElse(null);

        if (esc == null) {
            // Sin escenarios: default conservador.
            return new RespuestaComision(200, 0, new ComisionBody(
                    solicitud.referencia(), solicitud.concepto(),
                    new BigDecimal("2.50"), new BigDecimal("12.75"), "MXN", "CALCULADA"));
        }

        Object body = esc.esExito()
                ? new ComisionBody(solicitud.referencia(), solicitud.concepto(),
                        esc.porcentajeComision(), esc.montoComision(),
                        esc.moneda() != null ? esc.moneda() : "MXN",
                        esc.estatus() != null ? esc.estatus() : "CALCULADA")
                : new ErrorBody(esc.errorCodigo(), esc.errorMensaje());

        return new RespuestaComision(esc.httpStatus(), Math.max(0, esc.delayMs()), body);
    }
}
