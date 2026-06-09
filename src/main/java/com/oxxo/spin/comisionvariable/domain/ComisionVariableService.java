package com.oxxo.spin.comisionvariable.domain;

import com.oxxo.spin.comisionvariable.api.dto.ComisionRequest;
import com.oxxo.spin.comisionvariable.api.dto.ComisionResponse;
import com.oxxo.spin.comisionvariable.api.exception.SpinIntegrationException;
import com.oxxo.spin.comisionvariable.integration.spin.SpinClientService;
import com.oxxo.spin.comisionvariable.integration.spin.dto.SpinComisionRequest;
import com.oxxo.spin.comisionvariable.integration.spin.dto.SpinComisionResponse;
import com.oxxo.spin.comisionvariable.persistence.BitacoraService;
import com.oxxo.spin.comisionvariable.persistence.entity.BitacoraConsumo;
import io.smallrye.faulttolerance.api.RateLimitException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.jboss.logging.Logger;

import java.time.OffsetDateTime;

/**
 * Orquesta la consulta: arma el payload para Spin, invoca la fachada resiliente,
 * registra la bitacora (request + response + resultado + latencia) y mapea la
 * respuesta de salida. Toda falla deja rastro en bitacora.
 */
@ApplicationScoped
public class ComisionVariableService {

    private static final Logger LOG = Logger.getLogger(ComisionVariableService.class);

    @Inject
    SpinClientService spinClient;

    @Inject
    BitacoraService bitacora;

    public ComisionResponse consultar(ComisionRequest req) {
        SpinComisionRequest spinReq = new SpinComisionRequest(
                req.id(), req.plaza(), req.tienda(), req.caja(),
                req.transaccion(), req.producto(), req.servicio());

        BitacoraConsumo b = new BitacoraConsumo();
        b.consultaId = req.id();
        b.plaza = req.plaza();
        b.tienda = req.tienda();
        b.caja = req.caja();
        b.transaccion = req.transaccion();
        b.requestPayload = bitacora.toJson(spinReq);

        long t0 = System.nanoTime();
        try {
            SpinComisionResponse spinResp = spinClient.consultarComision(spinReq);

            b.responsePayload = bitacora.toJson(spinResp);
            b.httpStatus = 200;
            b.resultado = "OK";
            b.latenciaMs = (System.nanoTime() - t0) / 1_000_000;
            bitacora.registrar(b);

            return new ComisionResponse(
                    req.id(), req.plaza(), req.tienda(), req.caja(),
                    spinResp.concepto(),
                    spinResp.porcentajeComision(),
                    spinResp.montoComision(),
                    spinResp.moneda(),
                    OffsetDateTime.now());

        } catch (CircuitBreakerOpenException e) {
            registrarFallo(b, t0, "CIRCUIT_OPEN", "CIRCUIT_OPEN", 503, e.getMessage());
            throw new SpinIntegrationException("Spin no disponible (circuito abierto)", 503, e);
        } catch (RateLimitException e) {
            registrarFallo(b, t0, "RATE_LIMITED", "RATE_LIMITED", 429, e.getMessage());
            throw new SpinIntegrationException("Limite de peticiones a Spin excedido", 429, e);
        } catch (SpinIntegrationException e) {
            registrarFallo(b, t0, mapResultado(e.getStatus()), "SPIN_ERROR", e.getStatus(), e.getMessage());
            throw e;
        } catch (Exception e) {
            registrarFallo(b, t0, "ERROR", "UNEXPECTED", 502, e.getMessage());
            throw new SpinIntegrationException("Error inesperado consultando Spin", 502, e);
        }
    }

    private void registrarFallo(BitacoraConsumo b, long t0, String resultado,
                                String codigo, int status, String mensaje) {
        b.resultado = resultado;
        b.errorCodigo = codigo;
        b.httpStatus = status;
        b.errorMensaje = mensaje != null && mensaje.length() > 500 ? mensaje.substring(0, 500) : mensaje;
        b.latenciaMs = (System.nanoTime() - t0) / 1_000_000;
        bitacora.registrar(b);
        LOG.warnf("Consulta %s fallida: %s (%s)", b.consultaId, resultado, mensaje);
    }

    private String mapResultado(int status) {
        return status == 504 ? "TIMEOUT" : "ERROR";
    }
}
