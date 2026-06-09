package com.oxxo.spin.comisionvariable.adapter.out.spin.client;

import com.oxxo.spin.comisionvariable.adapter.out.spin.dto.SpinTokenResponse;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/** Cliente OAuth2 hacia Spin (obtencion de token). Config key: 'spin-auth'. */
@RegisterRestClient(configKey = "spin-auth")
@Path("/oauth")
public interface SpinAuthClient {

    @POST
    @Path("/token")
    @Produces(MediaType.APPLICATION_JSON)
    SpinTokenResponse token(
            @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret);
}
