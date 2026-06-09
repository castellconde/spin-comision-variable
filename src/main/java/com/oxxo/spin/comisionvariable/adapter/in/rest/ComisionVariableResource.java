package com.oxxo.spin.comisionvariable.adapter.in.rest;

import com.oxxo.spin.comisionvariable.adapter.in.rest.dto.ComisionRequest;
import com.oxxo.spin.comisionvariable.adapter.in.rest.dto.ComisionResponse;
import com.oxxo.spin.comisionvariable.adapter.in.rest.dto.ErrorResponse;
import com.oxxo.spin.comisionvariable.adapter.in.rest.mapper.ComisionRestMapper;
import com.oxxo.spin.comisionvariable.domain.model.Comision;
import com.oxxo.spin.comisionvariable.domain.port.in.ConsultarComisionUseCase;
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
 * Adaptador de ENTRADA (driving) REST. Depende del puerto de entrada
 * {@link ConsultarComisionUseCase}, nunca de la implementacion. Traduce DTOs
 * &lt;-&gt; modelos de dominio mediante {@link ComisionRestMapper}.
 */
@Path("/v1/comision-variable")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@SecurityRequirement(name = "keycloak")
@Tag(name = "Comision Variable", description = "Consulta de comision variable desde punto de venta")
public class ComisionVariableResource {

    @Inject
    ConsultarComisionUseCase consultarComision;

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
            @APIResponse(responseCode = "502", description = "Proveedor no disponible",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public Response consultar(@Valid ComisionRequest request) {
        Comision comision = consultarComision.consultar(ComisionRestMapper.toDomain(request));
        ComisionResponse response = ComisionRestMapper.toResponse(comision);
        return Response.ok(response)
                .location(UriBuilder.fromPath("/rest/v1/comision-variable/comision/{id}")
                        .build(response.id()))
                .build();
    }
}
