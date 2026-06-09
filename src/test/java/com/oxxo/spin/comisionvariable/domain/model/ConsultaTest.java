package com.oxxo.spin.comisionvariable.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsultaTest {

    private Consulta c(String producto, String servicio) {
        return new Consulta("POS-1", "PLZ-001", "TND-001", "CAJA-1", "TRX-1", producto, servicio);
    }

    @Test
    void tieneProductoOServicio_falsoSiAmbosVacios() {
        assertFalse(c(null, null).tieneProductoOServicio());
        assertFalse(c("", "  ").tieneProductoOServicio());
    }

    @Test
    void tieneProductoOServicio_verdaderoConProductoOServicio() {
        assertTrue(c("PROD-1", null).tieneProductoOServicio());
        assertTrue(c(null, "SERV-1").tieneProductoOServicio());
    }

    @Test
    void concepto_priorizaServicioSobreProducto() {
        assertEquals("SERV-1", c("PROD-1", "SERV-1").concepto());
        assertEquals("PROD-1", c("PROD-1", null).concepto());
        assertEquals("PROD-1", c("PROD-1", "").concepto());
    }
}
