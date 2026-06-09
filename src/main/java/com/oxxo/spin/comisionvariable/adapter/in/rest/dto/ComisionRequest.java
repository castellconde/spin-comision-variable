package com.oxxo.spin.comisionvariable.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO de entrada del adaptador REST. La validacion sintactica (regex, rangos,
 * obligatoriedad) es responsabilidad del adaptador, no del dominio.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ComisionRequest", description = "Datos de entrada para consultar la comision variable")
public record ComisionRequest(

        @Schema(description = "Identificador unico de la consulta", example = "POS-20260608-000123")
        @NotBlank(message = "El campo 'id' es obligatorio")
        @Size(min = 3, max = 100, message = "El campo 'id' debe tener entre 3 y 100 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9 ñÑ_-]+$", message = "El campo 'id' contiene caracteres no permitidos")
        String id,

        @Schema(description = "Clave de plaza", example = "PLZ-001")
        @NotBlank(message = "El campo 'plaza' es obligatorio")
        @Size(min = 3, max = 100, message = "El campo 'plaza' debe tener entre 3 y 100 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9 ñÑ_-]+$", message = "El campo 'plaza' contiene caracteres no permitidos")
        String plaza,

        @Schema(description = "Clave de tienda", example = "TND-04521")
        @NotBlank(message = "El campo 'tienda' es obligatorio")
        @Size(min = 3, max = 100, message = "El campo 'tienda' debe tener entre 3 y 100 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9 ñÑ_-]+$", message = "El campo 'tienda' contiene caracteres no permitidos")
        String tienda,

        @Schema(description = "Identificador de caja / punto de venta", example = "CAJA-03")
        @NotBlank(message = "El campo 'caja' es obligatorio")
        @Size(min = 3, max = 100, message = "El campo 'caja' debe tener entre 3 y 100 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9 ñÑ_-]+$", message = "El campo 'caja' contiene caracteres no permitidos")
        String caja,

        @Schema(description = "Folio o identificador de la transaccion", example = "TRX-998877")
        @NotBlank(message = "El campo 'transaccion' es obligatorio")
        @Size(min = 3, max = 100, message = "El campo 'transaccion' debe tener entre 3 y 100 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9 ñÑ_-]+$", message = "El campo 'transaccion' contiene caracteres no permitidos")
        String transaccion,

        @Schema(description = "Clave de producto (opcional si se envia servicio)", example = "PROD-7788")
        @Size(min = 3, max = 100, message = "El campo 'producto' debe tener entre 3 y 100 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9 ñÑ_-]+$", message = "El campo 'producto' contiene caracteres no permitidos")
        String producto,

        @Schema(description = "Clave de servicio (opcional si se envia producto)", example = "SERV-TAE-ATT")
        @Size(min = 3, max = 100, message = "El campo 'servicio' debe tener entre 3 y 100 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9 ñÑ_-]+$", message = "El campo 'servicio' contiene caracteres no permitidos")
        String servicio
) {
}
