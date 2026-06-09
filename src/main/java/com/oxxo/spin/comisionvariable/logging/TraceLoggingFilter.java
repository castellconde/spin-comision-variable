package com.oxxo.spin.comisionvariable.logging;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import org.jboss.logmanager.MDC;

import java.util.UUID;

/**
 * Filtro de trazabilidad: coloca trace_id / span_id / correlation_id en el MDC
 * para que TODO log JSON los incluya. Los campos viajan asi directo a Datadog
 * y permiten reconstruir la traza extremo a extremo de cada consulta del POS.
 *
 * <p>Si OpenTelemetry esta activo se reutiliza el trace_id real del span; de lo
 * contrario se genera uno local. El consumidor puede enviar su propio
 * {@code X-Correlation-Id}.</p>
 */
@Provider
public class TraceLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(TraceLoggingFilter.class);
    private static final String HEADER_CORRELATION = "X-Correlation-Id";

    @Override
    public void filter(ContainerRequestContext req) {
        SpanContext sc = Span.current().getSpanContext();
        String traceId = sc.isValid() ? sc.getTraceId() : UUID.randomUUID().toString().replace("-", "");
        String spanId = sc.isValid() ? sc.getSpanId() : traceId.substring(0, 16);

        String correlationId = req.getHeaderString(HEADER_CORRELATION);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = traceId;
        }

        MDC.put(TraceContext.TRACE_ID, traceId);
        MDC.put(TraceContext.SPAN_ID, spanId);
        MDC.put(TraceContext.CORRELATION_ID, correlationId);
        // Claves estandar Datadog para correlacion log<->trace
        MDC.put("dd.trace_id", traceId);
        MDC.put("dd.span_id", spanId);
        MDC.put("http.method", req.getMethod());
        MDC.put("http.path", req.getUriInfo().getPath());

        LOG.infof("Inicio request %s %s", req.getMethod(), req.getUriInfo().getPath());
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) {
        try {
            res.getHeaders().putSingle(HEADER_CORRELATION, MDC.get(TraceContext.CORRELATION_ID));
            LOG.infof("Fin request %s %s -> %d", req.getMethod(), req.getUriInfo().getPath(), res.getStatus());
        } finally {
            MDC.remove(TraceContext.TRACE_ID);
            MDC.remove(TraceContext.SPAN_ID);
            MDC.remove(TraceContext.CORRELATION_ID);
            MDC.remove("dd.trace_id");
            MDC.remove("dd.span_id");
            MDC.remove("http.method");
            MDC.remove("http.path");
        }
    }
}
