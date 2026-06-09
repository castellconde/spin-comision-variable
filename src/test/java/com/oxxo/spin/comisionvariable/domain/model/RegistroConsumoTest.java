package com.oxxo.spin.comisionvariable.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RegistroConsumoTest {

    private final Consulta consulta = new Consulta("POS-1", "PLZ", "TND", "CAJA", "TRX", null, "SERV");

    @Test
    void exito_armaRegistroOK() {
        Comision com = new Comision("POS-1", "PLZ", "TND", "CAJA", "SERV",
                new BigDecimal("2.5"), new BigDecimal("10"), "MXN", OffsetDateTime.now());
        RegistroConsumo r = RegistroConsumo.exito(consulta, com, 15);

        assertEquals(ResultadoConsulta.OK, r.resultado());
        assertEquals(200, r.httpStatus());
        assertEquals(15, r.latenciaMs());
        assertNull(r.errorCodigo());
        assertEquals(com, r.comision());
    }

    @Test
    void fallo_armaRegistroError() {
        RegistroConsumo r = RegistroConsumo.fallo(consulta, ResultadoConsulta.TIMEOUT,
                "SPIN_TIMEOUT", 504, "timeout", 6000);

        assertEquals(ResultadoConsulta.TIMEOUT, r.resultado());
        assertEquals(504, r.httpStatus());
        assertEquals("SPIN_TIMEOUT", r.errorCodigo());
        assertNull(r.comision());
    }
}
