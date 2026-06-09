package com.oxxo.spin.comisionvariable.adapter.out.spin;

import com.oxxo.spin.comisionvariable.domain.exception.ComisionProviderException;
import com.oxxo.spin.comisionvariable.domain.model.Comision;
import com.oxxo.spin.comisionvariable.domain.model.ResultadoConsulta;
import io.smallrye.faulttolerance.api.RateLimitException;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;

/**
 * Convierte las fallas tecnicas de fault tolerance (circuito abierto, rate
 * limit, timeout, etc.) en una {@link ComisionProviderException} de dominio.
 * Asi el nucleo nunca ve tipos de SmallRye/MicroProfile: la tecnologia de
 * resiliencia queda confinada al adaptador.
 */
@ApplicationScoped
public class SpinFallbackHandler implements FallbackHandler<Comision> {

    @Override
    public Comision handle(ExecutionContext context) {
        Throwable cause = context.getFailure();

        if (cause instanceof ComisionProviderException cpe) {
            throw cpe; // ya es de dominio, preservar codigo/status
        }
        if (cause instanceof CircuitBreakerOpenException) {
            throw new ComisionProviderException("SPIN_CIRCUIT_OPEN",
                    "Proveedor Spin no disponible (circuito abierto)", 503,
                    ResultadoConsulta.CIRCUIT_OPEN, cause);
        }
        if (cause instanceof RateLimitException) {
            throw new ComisionProviderException("SPIN_RATE_LIMITED",
                    "Limite de peticiones a Spin excedido", 429,
                    ResultadoConsulta.RATE_LIMITED, cause);
        }
        if (cause instanceof TimeoutException) {
            throw new ComisionProviderException("SPIN_TIMEOUT",
                    "Tiempo de espera agotado con Spin", 504,
                    ResultadoConsulta.TIMEOUT, cause);
        }
        throw new ComisionProviderException("SPIN_ERROR",
                "Error de comunicacion con Spin", 502,
                ResultadoConsulta.ERROR, cause);
    }
}
