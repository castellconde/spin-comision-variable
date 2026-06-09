package com.oxxo.spin.comisionvariable.infrastructure.logging;

import org.jboss.logmanager.MDC;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TraceContextTest {

    @AfterEach
    void clear() {
        MDC.remove(TraceContext.TRACE_ID);
        MDC.remove(TraceContext.CORRELATION_ID);
    }

    @Test
    void sinMdc_devuelveNa() {
        assertEquals("n/a", TraceContext.currentTraceId());
        assertEquals("n/a", TraceContext.currentCorrelationId());
    }

    @Test
    void conMdc_devuelveValores() {
        MDC.put(TraceContext.TRACE_ID, "abc123");
        MDC.put(TraceContext.CORRELATION_ID, "corr-9");
        assertEquals("abc123", TraceContext.currentTraceId());
        assertEquals("corr-9", TraceContext.currentCorrelationId());
    }
}
