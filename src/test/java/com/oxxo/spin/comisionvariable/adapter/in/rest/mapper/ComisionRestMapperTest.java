package com.oxxo.spin.comisionvariable.adapter.in.rest.mapper;

import com.oxxo.spin.comisionvariable.adapter.in.rest.dto.ComisionRequest;
import com.oxxo.spin.comisionvariable.adapter.in.rest.dto.ComisionResponse;
import com.oxxo.spin.comisionvariable.domain.model.Comision;
import com.oxxo.spin.comisionvariable.domain.model.Consulta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComisionRestMapperTest {

    @Test
    void toDomain_copiaTodosLosCampos() {
        ComisionRequest req = new ComisionRequest("POS-1", "PLZ", "TND", "CAJA", "TRX", "PROD", "SERV");
        Consulta c = ComisionRestMapper.toDomain(req);
        assertEquals("POS-1", c.id());
        assertEquals("PLZ", c.plaza());
        assertEquals("TND", c.tienda());
        assertEquals("CAJA", c.caja());
        assertEquals("TRX", c.transaccion());
        assertEquals("PROD", c.producto());
        assertEquals("SERV", c.servicio());
    }

    @Test
    void toResponse_copiaTodosLosCampos() {
        OffsetDateTime now = OffsetDateTime.now();
        Comision com = new Comision("POS-1", "PLZ", "TND", "CAJA", "SERV",
                new BigDecimal("2.5"), new BigDecimal("12.75"), "MXN", now);
        ComisionResponse r = ComisionRestMapper.toResponse(com);
        assertEquals("POS-1", r.id());
        assertEquals("SERV", r.concepto());
        assertEquals(new BigDecimal("12.75"), r.montoComision());
        assertEquals("MXN", r.moneda());
        assertEquals(now, r.timestamp());
    }
}
