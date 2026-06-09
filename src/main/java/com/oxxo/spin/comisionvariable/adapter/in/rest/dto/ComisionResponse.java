package com.oxxo.spin.comisionvariable.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** DTO de salida del adaptador REST. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ComisionResponse", description = "Resultado de la consulta de comision variable")
public record ComisionResponse(
        @Schema(example = "POS-20260608-000123") String id,
        @Schema(example = "PLZ-001") String plaza,
        @Schema(example = "TND-04521") String tienda,
        @Schema(example = "CAJA-03") String caja,
        @Schema(example = "SERV-TAE-ATT") String concepto,
        @Schema(example = "2.50") BigDecimal porcentajeComision,
        @Schema(example = "12.75") BigDecimal montoComision,
        @Schema(example = "MXN") String moneda,
        @Schema(example = "2026-06-08T10:15:30-06:00") OffsetDateTime timestamp) {
}
