package com.oxxo.spin.comisionvariable.adapter.out.spin.mapper;

import com.oxxo.spin.comisionvariable.adapter.out.spin.dto.SpinComisionRequest;
import com.oxxo.spin.comisionvariable.adapter.out.spin.dto.SpinComisionResponse;
import com.oxxo.spin.comisionvariable.domain.model.Comision;
import com.oxxo.spin.comisionvariable.domain.model.Consulta;

import java.time.OffsetDateTime;

/** Traduce entre el contrato de Spin y los modelos de dominio. */
public final class SpinMapper {

    private SpinMapper() {
    }

    public static SpinComisionRequest toSpin(Consulta c) {
        return new SpinComisionRequest(
                c.id(), c.plaza(), c.tienda(), c.caja(),
                c.transaccion(), c.producto(), c.servicio());
    }

    public static Comision toDomain(Consulta consulta, SpinComisionResponse resp) {
        return new Comision(
                consulta.id(), consulta.plaza(), consulta.tienda(), consulta.caja(),
                resp.concepto(),
                resp.porcentajeComision(),
                resp.montoComision(),
                resp.moneda(),
                OffsetDateTime.now());
    }
}
