package com.oxxo.spin.spinmock.adapter.in.rest;

import com.oxxo.spin.spinmock.domain.model.RespuestaComision;
import com.oxxo.spin.spinmock.domain.model.SolicitudComision;
import com.oxxo.spin.spinmock.domain.port.in.ResolverComisionUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.UUID;

/**
 * Adaptador de entrada que IMITA al API Spin. Las respuestas las decide el
 * dominio a partir de los escenarios en BD.
 */
@Path("/")
public class SpinMockResource {

    private static final Logger LOG = Logger.getLogger(SpinMockResource.class);

    @Inject
    ResolverComisionUseCase resolver;

    /** Emite un token OAuth2 mock (acepta cualquier credencial). */
    @POST
    @Path("/oauth/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> token() {
        return Map.of(
                "access_token", "mock-" + UUID.randomUUID(),
                "token_type", "Bearer",
                "expires_in", 300);
    }

    /** Calcula la comision segun el escenario que aplique. */
    @POST
    @Path("/v1/comisiones")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response comisiones(SolicitudComision solicitud) {
        RespuestaComision r = resolver.resolver(solicitud);
        if (r.delayMs() > 0) {
            try {
                Thread.sleep(r.delayMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        LOG.infof("Mock Spin: ref=%s -> http=%d (delay=%dms)",
                solicitud.referencia(), r.httpStatus(), r.delayMs());
        return Response.status(r.httpStatus()).entity(r.body()).build();
    }
}
