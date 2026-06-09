package com.oxxo.spin.comisionvariable.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

/** Estructura de error homogenea del adaptador REST (incluye traceId Datadog). */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ErrorResponse", description = "Estructura estandar de error")
public record ErrorResponse(
        @Schema(example = "VALIDATION_ERROR") String codigo,
        @Schema(example = "La solicitud contiene errores de validacion") String mensaje,
        @Schema(example = "400") int status,
        @Schema(example = "0af7651916cd43dd8448eb211c80319c") String traceId,
        OffsetDateTime timestamp,
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
