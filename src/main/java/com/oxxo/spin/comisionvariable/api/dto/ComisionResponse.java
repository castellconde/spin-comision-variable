package com.oxxo.spin.comisionvariable.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Contrato DUMMY de salida con la comision variable calculada por Spin.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ComisionResponse", description = "Resultado de la consulta de comision variable")
public record ComisionResponse(

        @Schema(description = "Eco del id de la consulta", example = "POS-20260608-000123")
        String id,

        @Schema(description = "Clave de plaza", example = "PLZ-001")
        String plaza,

        @Schema(description = "Clave de tienda", example = "TND-04521")
        String tienda,

        @Schema(description = "Identificador de caja", example = "CAJA-03")
        String caja,

        @Schema(description = "Producto o servicio evaluado", example = "SERV-TAE-ATT")
        String concepto,

        @Schema(description = "Porcentaje de comision aplicado", example = "2.50")
        BigDecimal porcentajeComision,

        @Schema(description = "Monto de comision calculado", example = "12.75")
        BigDecimal montoComision,

        @Schema(description = "Moneda ISO-4217", example = "MXN")
        String moneda,

        @Schema(description = "Marca de tiempo de la respuesta", example = "2026-06-08T10:15:30-06:00")
        OffsetDateTime timestamp
) {
}
