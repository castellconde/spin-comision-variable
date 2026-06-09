package com.oxxo.spin.comisionvariable.domain.exception;

import com.oxxo.spin.comisionvariable.domain.model.ResultadoConsulta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DomainExceptionsTest {

    @Test
    void businessException_exponeCodigoYStatus() {
        BusinessException ex = new BusinessException("CODE", "mensaje", 422);
        assertEquals("CODE", ex.getCodigo());
        assertEquals(422, ex.getStatus());
        assertEquals("mensaje", ex.getMessage());
    }

    @Test
    void comisionProviderException_exponeMetadatos() {
        Throwable cause = new RuntimeException("root");
        ComisionProviderException ex = new ComisionProviderException(
                "SPIN_CIRCUIT_OPEN", "abierto", 503, ResultadoConsulta.CIRCUIT_OPEN, cause);
        assertEquals("SPIN_CIRCUIT_OPEN", ex.getCodigo());
        assertEquals(503, ex.getStatus());
        assertEquals(ResultadoConsulta.CIRCUIT_OPEN, ex.getResultado());
        assertSame(cause, ex.getCause());
    }
}
