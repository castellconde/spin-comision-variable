package com.oxxo.spin.comisionvariable.api;

import com.oxxo.spin.comisionvariable.api.dto.ComisionRequest;
import com.oxxo.spin.comisionvariable.api.dto.ComisionResponse;
import com.oxxo.spin.comisionvariable.api.dto.ErrorResponse;
import com.oxxo.spin.comisionvariable.api.exception.BusinessException;
import com.oxxo.spin.comisionvariable.domain.ComisionVariableService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Endpoint de consulta de Comision Variable.
 * Protegido por Keycloak (realm de aplicaciones) - requiere Bearer token valido.
 */
@Path("/v1/comision-variable")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@SecurityRequirement(name = "keycloak")
@Tag(name = "Comision Variable", description = "Consulta de comision variable desde punto de venta")
public class ComisionVariableResource {

    @Inject
    ComisionVariableService service;

    @POST
    @Path("/comision")
    @Operation(summary = "Consulta la comision variable de un producto o servicio")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Comision calculada",
                    content = @Content(schema = @Schema(implementation = ComisionResponse.class))),
            @APIResponse(responseCode = "400", description = "Request invalido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "401", description = "No autenticado"),
            @APIResponse(responseCode = "422", description = "Regla de negocio incumplida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @APIResponse(responseCode = "502", description = "Spin no disponible",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response consultar(@Valid ComisionRequest request) {
        // Regla de negocio adicional: producto y/o servicio
        if (!request.tieneProductoOServicio()) {
            throw new BusinessException(
                    "PRODUCTO_O_SERVICIO_REQUERIDO",
                    "Debe indicar al menos 'producto' o 'servicio'",
                    422);
        }

        ComisionResponse response = service.consultar(request);

        // 200 + Location header (estilo HU-001)
        return Response.ok(response)
                .location(UriBuilder.fromPath("/rest/v1/comision-variable/comision/{id}")
                        .build(response.id()))
                .build();
    }
}
