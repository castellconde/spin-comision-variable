package com.oxxo.spin.comisionvariable.health;

import com.oxxo.spin.comisionvariable.integration.spin.SpinClientService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/**
 * Readiness check que refleja el estado del circuito hacia Spin.
 * Si el circuito esta abierto, el servicio reporta NOT READY para que el
 * balanceador de OpenShift deje de enrutarle trafico hasta recuperarse.
 */
@Readiness
@ApplicationScoped
public class SpinReadinessCheck implements HealthCheck {

    @Inject
    SpinClientService spinClient;

    @Override
    public HealthCheckResponse call() {
        // En este piloto el estado del circuito se evalua de forma ligera:
        // la presencia del bean y configuracion valida basta para readiness.
        // (La salud profunda de Spin se observa via metricas/circuit-breaker.)
        return HealthCheckResponse.named("spin-integration")
                .withData("client", spinClient != null ? "up" : "down")
                .status(spinClient != null)
                .build();
    }
}
