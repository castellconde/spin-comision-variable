package com.oxxo.spin.comisionvariable.api.exception;

import com.oxxo.spin.comisionvariable.api.dto.ErrorResponse;
import com.oxxo.spin.comisionvariable.logging.TraceContext;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Control de errores centralizado (estilo microservicio receiver de Oxxocel):
 * toda excepcion se traduce a un {@link ErrorResponse} homogeneo con traceId
 * para Datadog. Evita filtrar stacktraces al consumidor.
 */
public class ExceptionMappers {

    private static final Logger LOG = Logger.getLogger(ExceptionMappers.class);

    /** Errores de Bean Validation (request invalido) -> 400. */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> mapValidation(ConstraintViolationException ex) {
        List<ErrorResponse.FieldError> detalles = ex.getConstraintViolations().stream()
                .map(v -> new ErrorResponse.FieldError(ultimoNodo(v.getPropertyPath().toString()), v.getMessage()))
                .toList();
        String traceId = TraceContext.currentTraceId();
        LOG.infof("Request invalido [trace=%s]: %s", traceId, detalles);
        ErrorResponse body = new ErrorResponse(
                "VALIDATION_ERROR",
                "La solicitud contiene errores de validacion",
                Response.Status.BAD_REQUEST.getStatusCode(),
                traceId,
                OffsetDateTime.now(),
                detalles);
        return RestResponse.status(Response.Status.BAD_REQUEST, body);
    }

    /** Reglas de negocio. */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> mapBusiness(BusinessException ex) {
        String traceId = TraceContext.currentTraceId();
        LOG.warnf("Error de negocio [trace=%s] %s: %s", traceId, ex.getCodigo(), ex.getMessage());
        ErrorResponse body = ErrorResponse.of(ex.getCodigo(), ex.getMessage(), ex.getStatus(), traceId);
        return RestResponse.ResponseBuilder.create(ex.getStatus(), body).build();
    }

    /** Falla del API externo Spin. */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> mapSpin(SpinIntegrationException ex) {
        String traceId = TraceContext.currentTraceId();
        LOG.errorf(ex, "Falla integracion Spin [trace=%s]: %s", traceId, ex.getMessage());
        ErrorResponse body = ErrorResponse.of(
                "SPIN_UNAVAILABLE",
                "El proveedor Spin no esta disponible. Intente mas tarde.",
                ex.getStatus(),
                traceId);
        return RestResponse.ResponseBuilder.create(ex.getStatus(), body).build();
    }

    /** Catch-all -> 500 sin filtrar detalles internos. */
    @ServerExceptionMapper
    public RestResponse<ErrorResponse> mapGeneric(Exception ex) {
        String traceId = TraceContext.currentTraceId();
        LOG.errorf(ex, "Error no controlado [trace=%s]", traceId);
        ErrorResponse body = ErrorResponse.of(
                "INTERNAL_ERROR",
                "Ocurrio un error inesperado. Refiera el traceId a soporte.",
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                traceId);
        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, body);
    }

    private static String ultimoNodo(String path) {
        int i = path.lastIndexOf('.');
        return i >= 0 ? path.substring(i + 1) : path;
    }
}
