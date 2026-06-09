package com.oxxo.spin.comisionvariable.adapter.out.spin.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Configuracion tipada de la integracion con Spin. Pertenece al adaptador de
 * salida (detalle de infraestructura), no al dominio. Los secretos llegan por
 * variable de ambiente (Conjur).
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
}
