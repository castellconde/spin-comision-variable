package com.oxxo.spin.comisionvariable.adapter.out.spin.client;

import com.oxxo.spin.comisionvariable.adapter.out.spin.dto.SpinComisionRequest;
import com.oxxo.spin.comisionvariable.adapter.out.spin.dto.SpinComisionResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/** Cliente del API de negocio de Spin. Bearer (OAuth2) + apikey. Config key: 'spin-api'. */
@RegisterRestClient(configKey = "spin-api")
@Path("/v1/comisiones")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SpinApiClient {

    @POST
    SpinComisionResponse calcularComision(
            @HeaderParam("Authorization") String bearer,
            @HeaderParam("x-api-key") String apiKey,
            SpinComisionRequest body);
}
