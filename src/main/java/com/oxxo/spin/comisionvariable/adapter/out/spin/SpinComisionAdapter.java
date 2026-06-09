package com.oxxo.spin.comisionvariable.adapter.out.spin;

import com.oxxo.spin.comisionvariable.adapter.out.spin.client.SpinApiClient;
import com.oxxo.spin.comisionvariable.adapter.out.spin.config.SpinConfig;
import com.oxxo.spin.comisionvariable.adapter.out.spin.dto.SpinComisionResponse;
import com.oxxo.spin.comisionvariable.adapter.out.spin.mapper.SpinMapper;
import com.oxxo.spin.comisionvariable.domain.exception.ComisionProviderException;
import com.oxxo.spin.comisionvariable.domain.model.Comision;
import com.oxxo.spin.comisionvariable.domain.model.Consulta;
import com.oxxo.spin.comisionvariable.domain.model.ResultadoConsulta;
import com.oxxo.spin.comisionvariable.domain.port.out.ComisionProviderPort;
import io.smallrye.faulttolerance.api.RateLimit;
import io.smallrye.faulttolerance.api.RateLimitException;
import io.smallrye.faulttolerance.api.RateLimitType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.temporal.ChronoUnit;

/**
 * Adaptador de SALIDA (driven) que implementa {@link ComisionProviderPort}
 * sobre el API de Spin. Aqui residen TODAS las politicas de resiliencia
 * (reintentos, circuit breaker, timeout, rate limit), configurables por
 * variable de ambiente. Las fallas tecnicas se traducen a excepciones de
 * dominio via {@link SpinFallbackHandler}, de modo que el nucleo no conoce
 * SmallRye/MicroProfile.
 *
 * <p>El nombre del metodo {@code obtenerComision} es la clave usada por las
 * propiedades de fault-tolerance para sobre-escribir los valores.</p>
 */
@ApplicationScoped
public class SpinComisionAdapter implements ComisionProviderPort {

    private static final Logger LOG = Logger.getLogger(SpinComisionAdapter.class);

    @Inject
    @RestClient
    SpinApiClient apiClient;

    @Inject
    SpinAuthService authService;

    @Inject
    SpinConfig spinConfig;

    @Override
    @Retry(
            maxRetries = 3,
            delay = 300,
            jitter = 100,
            retryOn = ComisionProviderException.class,
            abortOn = RateLimitException.class)
    @CircuitBreaker(
            requestVolumeThreshold = 8,
            failureRatio = 0.5,
            delay = 5000,
            successThreshold = 2,
            failOn = ComisionProviderException.class)
    @Timeout(value = 6, unit = ChronoUnit.SECONDS)
    @RateLimit(
            value = 50,
            window = 1,
            windowUnit = ChronoUnit.SECONDS,
            type = RateLimitType.ROLLING)
    @Fallback(SpinFallbackHandler.class)
    public Comision obtenerComision(Consulta consulta) {
        try {
            SpinComisionResponse resp = apiClient.calcularComision(
                    authService.bearerToken(),
                    spinConfig.apiKey(),
                    SpinMapper.toSpin(consulta));
            return SpinMapper.toDomain(consulta, resp);
        } catch (WebApplicationException wae) {
            int code = wae.getResponse() != null ? wae.getResponse().getStatus() : 502;
            if (code == 401) {
                LOG.warn("Spin respondio 401, invalidando token cacheado");
                authService.invalidate();
            }
            throw new ComisionProviderException("SPIN_HTTP_" + code,
                    "Spin respondio HTTP " + code, code >= 500 ? 502 : code,
                    ResultadoConsulta.ERROR, wae);
        }
    }
}
