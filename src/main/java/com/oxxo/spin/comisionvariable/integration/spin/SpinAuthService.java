package com.oxxo.spin.comisionvariable.integration.spin;

import com.oxxo.spin.comisionvariable.api.exception.SpinIntegrationException;
import com.oxxo.spin.comisionvariable.cache.TokenCacheService;
import com.oxxo.spin.comisionvariable.config.SpinConfig;
import com.oxxo.spin.comisionvariable.integration.spin.dto.SpinTokenResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Gestiona el token OAuth2 de Spin: lo cachea (Redis opcional) y lo renueva
 * cuando expira. El error de autenticacion tambien aplica reintentos.
 */
@ApplicationScoped
public class SpinAuthService {

    private static final Logger LOG = Logger.getLogger(SpinAuthService.class);
    private static final long EXPIRY_MARGIN_SECONDS = 30;

    @Inject
    @RestClient
    SpinAuthClient authClient;

    @Inject
    SpinConfig spinConfig;

    @Inject
    TokenCacheService tokenCache;

    /** Devuelve un Bearer valido (cacheado o recien emitido). */
    public String bearerToken() {
        return tokenCache.get()
                .map(t -> "Bearer " + t)
                .orElseGet(() -> "Bearer " + renew());
    }

    public void invalidate() {
        tokenCache.invalidate();
    }

    @Retry(maxRetries = 2, delay = 200, jitter = 100, retryOn = Exception.class)
    @Timeout(value = 4, unit = ChronoUnit.SECONDS)
    String renew() {
        try {
            SpinTokenResponse resp = authClient.token(
                    spinConfig.tokenGrantType(),
                    spinConfig.clientId(),
                    spinConfig.clientSecret());
            long ttl = Math.max(1, resp.expiresIn() - EXPIRY_MARGIN_SECONDS);
            tokenCache.put(resp.accessToken(), Duration.of(ttl, ChronoUnit.SECONDS));
            LOG.debugf("Token Spin renovado (ttl=%ds)", ttl);
            return resp.accessToken();
        } catch (Exception e) {
            throw new SpinIntegrationException("No fue posible obtener token de Spin", 502, e);
        }
    }
}
