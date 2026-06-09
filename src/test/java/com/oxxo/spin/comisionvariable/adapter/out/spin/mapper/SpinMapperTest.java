package com.oxxo.spin.comisionvariable.adapter.out.spin.mapper;

import com.oxxo.spin.comisionvariable.adapter.out.spin.dto.SpinComisionRequest;
import com.oxxo.spin.comisionvariable.adapter.out.spin.dto.SpinComisionResponse;
import com.oxxo.spin.comisionvariable.domain.model.Comision;
import com.oxxo.spin.comisionvariable.domain.model.Consulta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SpinMapperTest {

    private final Consulta consulta = new Consulta("POS-1", "PLZ", "TND", "CAJA", "TRX", "PROD", "SERV");

    @Test
    void toSpin_mapeaReferenciaYCampos() {
        SpinComisionRequest req = SpinMapper.toSpin(consulta);
        assertEquals("POS-1", req.referencia());
        assertEquals("PLZ", req.plaza());
        assertEquals("PROD", req.producto());
        assertEquals("SERV", req.servicio());
    }

    @Test
    void toDomain_combinaConsultaYRespuesta() {
        SpinComisionResponse resp = new SpinComisionResponse(
                "POS-1", "SERV", new BigDecimal("2.5"), new BigDecimal("12.75"), "MXN", "CALCULADA");
        Comision c = SpinMapper.toDomain(consulta, resp);
        assertEquals("POS-1", c.id());
        assertEquals("PLZ", c.plaza());
        assertEquals("SERV", c.concepto());
        assertEquals(new BigDecimal("12.75"), c.montoComision());
        assertEquals("MXN", c.moneda());
        assertNotNull(c.calculadaEn());
    }
}
