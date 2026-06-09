package com.oxxo.spin.comisionvariable.integration.spin;

import com.oxxo.spin.comisionvariable.api.exception.SpinIntegrationException;
import com.oxxo.spin.comisionvariable.config.SpinConfig;
import com.oxxo.spin.comisionvariable.integration.spin.dto.SpinComisionRequest;
import com.oxxo.spin.comisionvariable.integration.spin.dto.SpinComisionResponse;
import io.smallrye.faulttolerance.api.RateLimit;
import io.smallrye.faulttolerance.api.RateLimitException;
import io.smallrye.faulttolerance.api.RateLimitType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.temporal.ChronoUnit;

/**
 * Fachada resiliente hacia el API de Spin (estilo servicio Transactions de
 * Oxxocel): combina reintentos implicitos, circuit breaker, timeout y rate
 * limit. Todos los parametros son configurables por properties / variables de
 * ambiente (ver application.properties, prefijo quarkus.fault-tolerance...).
 *
 * <p>IMPORTANTE: el nombre del metodo {@code consultarComision} es la clave que
 * usan las propiedades de fault-tolerance para sobre-escribir los valores sin
 * recompilar.</p>
 */
@ApplicationScoped
public class SpinClientService {

    private static final Logger LOG = Logger.getLogger(SpinClientService.class);

    @Inject
    @RestClient
    SpinApiClient apiClient;

    @Inject
    SpinAuthService authService;

    @Inject
    SpinConfig spinConfig;

    /**
     * Consulta la comision en Spin con politica de resiliencia completa.
     * Los valores anotados son DEFAULTS; se sobre-escriben en application.properties.
     */
    @Retry(
            maxRetries = 3,
            delay = 300,
            jitter = 100,
            retryOn = {SpinIntegrationException.class, RuntimeException.class},
            abortOn = {RateLimitException.class})
    @CircuitBreaker(
            requestVolumeThreshold = 8,
            failureRatio = 0.5,
            delay = 5000,
            successThreshold = 2)
    @Timeout(value = 6, unit = ChronoUnit.SECONDS)
    @RateLimit(
            value = 50,
            window = 1,
            windowUnit = ChronoUnit.SECONDS,
            type = RateLimitType.ROLLING)
    public SpinComisionResponse consultarComision(SpinComisionRequest request) {
        try {
            return apiClient.calcularComision(
                    authService.bearerToken(),
                    spinConfig.apiKey(),
                    request);
        } catch (jakarta.ws.rs.WebApplicationException wae) {
            int code = wae.getResponse() != null ? wae.getResponse().getStatus() : 502;
            // 401 -> token invalido: invalidar cache para forzar renovacion en el retry
            if (code == 401) {
                LOG.warn("Spin respondio 401, invalidando token cacheado");
                authService.invalidate();
            }
            throw new SpinIntegrationException("Spin respondio HTTP " + code, code >= 500 ? 502 : code, wae);
        } catch (Exception e) {
            throw new SpinIntegrationException("Error de comunicacion con Spin", 502, e);
        }
    }
}
