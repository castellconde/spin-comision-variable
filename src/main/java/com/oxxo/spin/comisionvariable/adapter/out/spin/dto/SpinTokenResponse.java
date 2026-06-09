package com.oxxo.spin.comisionvariable.adapter.out.spin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Respuesta OAuth2 del proveedor Spin. */
public record SpinTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") long expiresIn) {
}
