package com.oxxo.spin.comisionvariable.infrastructure.health;

import com.oxxo.spin.comisionvariable.domain.port.out.ComisionProviderPort;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class HealthChecksTest {

    @Test
    void liveness_estaUp() {
        HealthCheckResponse r = new LivenessCheck().call();
        assertEquals(HealthCheckResponse.Status.UP, r.getStatus());
    }

    @Test
    void readiness_estaUpCuandoElPuertoEstaCableado() {
        SpinReadinessCheck check = new SpinReadinessCheck();
        check.comisionProvider = mock(ComisionProviderPort.class);
        HealthCheckResponse r = check.call();
        assertEquals(HealthCheckResponse.Status.UP, r.getStatus());
        assertEquals("comision-provider", r.getName());
    }
}
