package com.oxxo.spin.comisionvariable.adapter.in.rest.mapper;

import com.oxxo.spin.comisionvariable.adapter.in.rest.dto.ComisionRequest;
import com.oxxo.spin.comisionvariable.adapter.in.rest.dto.ComisionResponse;
import com.oxxo.spin.comisionvariable.domain.model.Comision;
import com.oxxo.spin.comisionvariable.domain.model.Consulta;

/**
 * Traduce entre los DTOs del borde REST y los modelos de dominio.
 * Mantiene el nucleo libre de Jackson/OpenAPI/Validation.
 */
public final class ComisionRestMapper {

    private ComisionRestMapper() {
    }

    public static Consulta toDomain(ComisionRequest req) {
        return new Consulta(
                req.id(), req.plaza(), req.tienda(), req.caja(),
                req.transaccion(), req.producto(), req.servicio());
    }

    public static ComisionResponse toResponse(Comision c) {
        return new ComisionResponse(
                c.id(), c.plaza(), c.tienda(), c.caja(), c.concepto(),
                c.porcentajeComision(), c.montoComision(), c.moneda(), c.calculadaEn());
    }
}
