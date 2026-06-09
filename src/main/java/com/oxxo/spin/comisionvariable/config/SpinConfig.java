package com.oxxo.spin.comisionvariable.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Configuracion tipada de la integracion con Spin.
 * Los secretos llegan por variable de ambiente (Conjur) y se resuelven en
 * application.properties; aqui se exponen de forma fuertemente tipada.
 */
@ConfigMapping(prefix = "spin")
public interface SpinConfig {

    @WithName("client-id")
    String clientId();

    @WithName("client-secret")
    String clientSecret();

    @WithName("api-key")
    String apiKey();

    @WithName("token-grant-type")
    @WithDefault("client_credentials")
    String tokenGrantType();

    RateLimit rateLimit();

    interface RateLimit {
        @WithDefault("true")
        boolean enabled();

        @WithDefault("50")
        int value();

        @WithName("window-ms")
        @WithDefault("1000")
        long windowMs();

        @WithName("min-spacing-ms")
        @WithDefault("0")
        long minSpacingMs();
    }
}
