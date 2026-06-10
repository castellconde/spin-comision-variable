package com.oxxo.spin.spinmock.adapter.in.rest;

import com.oxxo.spin.spinmock.domain.model.Escenario;
import com.oxxo.spin.spinmock.domain.port.in.AdministrarEscenariosUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * CRUD de escenarios del mock (administracion en BD).
 * GET/POST/PUT/DELETE sobre /admin/escenarios.
 */
@Path("/admin/escenarios")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EscenarioAdminResource {

    @Inject
    AdministrarEscenariosUseCase admin;

    @GET
    public List<Escenario> listar() {
        return admin.listar();
    }

    @GET
    @Path("/{id}")
    public Response porId(@PathParam("id") Long id) {
        return admin.porId(id)
                .map(e -> Response.ok(e).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    public Response crear(Escenario escenario) {
        Escenario creado = admin.crear(escenario);
        return Response.status(Response.Status.CREATED).entity(creado).build();
    }

    @PUT
    @Path("/{id}")
    public Response actualizar(@PathParam("id") Long id, Escenario escenario) {
        return admin.actualizar(id, escenario)
                .map(e -> Response.ok(e).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{id}")
    public Response borrar(@PathParam("id") Long id) {
        return admin.borrar(id)
                ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }
}
