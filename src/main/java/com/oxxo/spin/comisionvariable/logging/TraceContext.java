package com.oxxo.spin.comisionvariable.logging;

import org.jboss.logmanager.MDC;

/**
 * Acceso al identificador de traza actual para correlacionar respuestas
 * de error con los logs en Datadog. Se alimenta de la clave {@code trace_id}
 * del MDC que coloca {@link TraceLoggingFilter}.
 */
public final class TraceContext {

    public static final String TRACE_ID = "trace_id";
    public static final String SPAN_ID = "span_id";
    public static final String CORRELATION_ID = "correlation_id";

    private TraceContext() {
    }

    public static String currentTraceId() {
        String t = MDC.get(TRACE_ID);
        return t != null ? t : "n/a";
    }

    public static String currentCorrelationId() {
        String c = MDC.get(CORRELATION_ID);
        return c != null ? c : "n/a";
    }
}
