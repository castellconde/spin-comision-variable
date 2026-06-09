package com.oxxo.spin.comisionvariable.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Estructura de error homogenea para todas las respuestas no-2xx.
 * Incluye {@code traceId} para correlacionar con los logs en Datadog.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ErrorResponse", description = "Estructura estandar de error")
public record ErrorResponse(
        @Schema(description = "Codigo de error de negocio", example = "VALIDATION_ERROR")
        String codigo,

        @Schema(description = "Mensaje legible", example = "La solicitud contiene errores de validacion")
        String mensaje,

        @Schema(description = "Codigo HTTP", example = "400")
        int status,

        @Schema(description = "Id de traza para soporte / Datadog", example = "0af7651916cd43dd8448eb211c80319c")
        String traceId,

        @Schema(description = "Marca de tiempo")
        OffsetDateTime timestamp,

        @Schema(description = "Detalle por campo (cuando aplica)")
        List<FieldError> errores
) {
    @Schema(name = "FieldError", description = "Error de validacion de un campo")
    public record FieldError(
            @Schema(example = "plaza") String campo,
            @Schema(example = "El campo 'plaza' es obligatorio") String mensaje) {
    }

    public static ErrorResponse of(String codigo, String mensaje, int status, String traceId) {
        return new ErrorResponse(codigo, mensaje, status, traceId, OffsetDateTime.now(), null);
    }
}
