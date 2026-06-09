package com.oxxo.spin.comisionvariable.adapter.out.spin;

import com.oxxo.spin.comisionvariable.domain.exception.ComisionProviderException;
import com.oxxo.spin.comisionvariable.domain.model.ResultadoConsulta;
import io.smallrye.faulttolerance.api.RateLimitException;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpinFallbackHandlerTest {

    private final SpinFallbackHandler handler = new SpinFallbackHandler();

    private ExecutionContext ctxConFalla(Throwable t) {
        ExecutionContext ctx = Mockito.mock(ExecutionContext.class);
        Mockito.when(ctx.getFailure()).thenReturn(t);
        return ctx;
    }

    @Test
    void circuitoAbierto_seTraduceA503() {
        ComisionProviderException ex = assertThrows(ComisionProviderException.class,
                () -> handler.handle(ctxConFalla(new CircuitBreakerOpenException("open"))));
        assertEquals(503, ex.getStatus());
        assertEquals(ResultadoConsulta.CIRCUIT_OPEN, ex.getResultado());
    }

    @Test
    void rateLimit_seTraduceA429() {
        ComisionProviderException ex = assertThrows(ComisionProviderException.class,
                () -> handler.handle(ctxConFalla(Mockito.mock(RateLimitException.class))));
        assertEquals(429, ex.getStatus());
        assertEquals(ResultadoConsulta.RATE_LIMITED, ex.getResultado());
    }

    @Test
    void timeout_seTraduceA504() {
        ComisionProviderException ex = assertThrows(ComisionProviderException.class,
                () -> handler.handle(ctxConFalla(new TimeoutException("t"))));
        assertEquals(504, ex.getStatus());
        assertEquals(ResultadoConsulta.TIMEOUT, ex.getResultado());
    }

    @Test
    void excepcionDeDominio_sePreserva() {
        ComisionProviderException original = new ComisionProviderException(
                "SPIN_HTTP_400", "bad", 400, ResultadoConsulta.ERROR, null);
        ComisionProviderException ex = assertThrows(ComisionProviderException.class,
                () -> handler.handle(ctxConFalla(original)));
        assertEquals("SPIN_HTTP_400", ex.getCodigo());
        assertEquals(400, ex.getStatus());
    }

    @Test
    void otraFalla_seTraduceA502() {
        ComisionProviderException ex = assertThrows(ComisionProviderException.class,
                () -> handler.handle(ctxConFalla(new RuntimeException("boom"))));
        assertEquals(502, ex.getStatus());
        assertEquals(ResultadoConsulta.ERROR, ex.getResultado());
    }
}
