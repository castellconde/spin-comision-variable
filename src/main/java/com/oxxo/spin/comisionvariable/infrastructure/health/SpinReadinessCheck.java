package com.oxxo.spin.comisionvariable.infrastructure.health;

import com.oxxo.spin.comisionvariable.domain.port.out.ComisionProviderPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Readiness: verifica que el puerto del proveedor de comisiones este cableado.
 * La salud profunda del proveedor se observa via el circuit breaker / metricas.
 */
@Readiness
@ApplicationScoped
public class SpinReadinessCheck implements HealthCheck {

    @Inject
    ComisionProviderPort comisionProvider;

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("comision-provider")
                .withData("port", comisionProvider != null ? "up" : "down")
                .status(comisionProvider != null)
                .build();
    }
}
